package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeDTO;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeKilometrageAdminCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeKilometrageCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiDTO;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeUpdateRequest;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
import bzh.stack.apiavtrans.mapper.VehiculeKilometrageMapper;
import bzh.stack.apiavtrans.mapper.VehiculeMapper;
import bzh.stack.apiavtrans.repository.VehiculeKilometrageRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Véhicule relais vu du véhicule : rattachement des relevés, kilométrage courant, ancienne plaque.
 */
@ExtendWith(MockitoExtension.class)
class VehiculeServiceRelaiTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private VehiculeKilometrageRepository vehiculeKilometrageRepository;
    @Mock
    private VehiculeKilometrageMapper vehiculeKilometrageMapper;
    @Mock
    private VehiculeRelaiService vehiculeRelaiService;
    @Spy
    private VehiculeMapper vehiculeMapper = new VehiculeMapper();

    @InjectMocks
    private VehiculeService vehiculeService;

    private Vehicule vehicule;
    private VehiculeRelai relai;

    @BeforeEach
    void setUp() {
        vehicule = new Vehicule();
        vehicule.setId(UUID.randomUUID());
        vehicule.setImmat("AB-123-CD");

        relai = new VehiculeRelai();
        relai.setId(UUID.randomUUID());
        relai.setVehicule(vehicule);
        relai.setImmat("EF-456-GH");
        relai.setDateDebut(LocalDate.now(PARIS).minusDays(3));
    }

    @Test
    void should_attach_reading_to_running_relay_when_driver_adds_kilometrage() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiService.relaiActifLe(vehicule, LocalDate.now(PARIS))).thenReturn(relai);
        when(vehiculeKilometrageRepository.save(any(VehiculeKilometrage.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeKilometrageCreateRequest request = new VehiculeKilometrageCreateRequest();
        request.setVehiculeId(vehicule.getId());
        request.setKm(45210);
        vehiculeService.addVehiculeKilometrage(request, new User());

        assertThat(savedKilometrage().getRelai()).isEqualTo(relai);
    }

    @Test
    void should_keep_reading_on_vehicle_when_no_relay_is_running() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeKilometrageRepository.save(any(VehiculeKilometrage.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeKilometrageCreateRequest request = new VehiculeKilometrageCreateRequest();
        request.setVehiculeId(vehicule.getId());
        request.setKm(125000);
        vehiculeService.addVehiculeKilometrage(request, new User());

        assertThat(savedKilometrage().getRelai()).isNull();
    }

    @Test
    void should_attach_reading_by_its_date_when_admin_adds_past_kilometrage() {
        ZonedDateTime createdAt = ZonedDateTime.of(2026, 9, 15, 23, 30, 0, 0, ZoneId.of("UTC"));
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        // 23 h 30 UTC = 1 h 30 le 16 à Paris
        when(vehiculeRelaiService.relaiActifLe(vehicule, LocalDate.of(2026, 9, 16))).thenReturn(relai);
        when(vehiculeKilometrageRepository.save(any(VehiculeKilometrage.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeKilometrageAdminCreateRequest request = new VehiculeKilometrageAdminCreateRequest();
        request.setVehiculeId(vehicule.getId());
        request.setKm(45300);
        request.setCreatedAt(createdAt);
        vehiculeService.addVehiculeKilometrageAsAdmin(request);

        assertThat(savedKilometrage().getRelai()).isEqualTo(relai);
    }

    @Test
    void should_use_relay_km_as_current_km_when_relay_is_running() {
        VehiculeKilometrage propre = new VehiculeKilometrage();
        propre.setKm(125000);
        propre.setCreatedAt(ZonedDateTime.now(PARIS).minusDays(4));
        VehiculeRelaiDTO relaiDTO = new VehiculeRelaiDTO();
        relaiDTO.setImmat("EF-456-GH");
        relaiDTO.setDateDebut(relai.getDateDebut());
        relaiDTO.setKmDebut(45000);
        relaiDTO.setLatestKm(45210);
        relaiDTO.setLatestKmDate(ZonedDateTime.now(PARIS));

        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiService.relaiActifLe(eq(vehicule), any(LocalDate.class))).thenReturn(relai);
        when(vehiculeRelaiService.toDTO(relai)).thenReturn(relaiDTO);
        when(vehiculeKilometrageRepository.findLatestByVehicule(vehicule)).thenReturn(Optional.of(propre));

        VehiculeDTO dto = vehiculeService.getVehiculeById(vehicule.getId());

        assertThat(dto.getLatestKm()).isEqualTo(45210);
        assertThat(dto.getVehiculeLatestKm()).isEqualTo(125000);
        assertThat(dto.getRelaiEnCours()).isSameAs(relaiDTO);
        assertThat(dto.getRelaiImmat()).isEqualTo("EF-456-GH");
    }

    @Test
    void should_use_relay_start_km_when_relay_has_no_reading_yet() {
        VehiculeRelaiDTO relaiDTO = new VehiculeRelaiDTO();
        relaiDTO.setImmat("EF-456-GH");
        relaiDTO.setDateDebut(LocalDate.of(2026, 9, 14));
        relaiDTO.setKmDebut(45000);

        VehiculeDTO dto = vehiculeMapper.toDTO(vehicule, null, relaiDTO);

        assertThat(dto.getLatestKm()).isEqualTo(45000);
        assertThat(dto.getLatestKmDate()).isEqualTo(LocalDate.of(2026, 9, 14).atStartOfDay(PARIS));
        assertThat(dto.getVehiculeLatestKm()).isNull();
    }

    @Test
    void should_ignore_legacy_relay_plate_when_vehicle_has_relays() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiService.aDesRelais(vehicule)).thenReturn(true);
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(inv -> inv.getArgument(0));

        vehiculeService.updateVehicule(vehicule.getId(), updateRequest("EF-456-GH"));

        assertThat(vehicule.getRelaiImmat()).isNull();
    }

    @Test
    void should_keep_legacy_relay_plate_when_vehicle_has_no_relays() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiService.aDesRelais(vehicule)).thenReturn(false);
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(inv -> inv.getArgument(0));

        vehiculeService.updateVehicule(vehicule.getId(), updateRequest("OLD-000"));

        assertThat(vehicule.getRelaiImmat()).isEqualTo("OLD-000");
    }

    @Test
    void should_detach_readings_from_relays_before_deleting_vehicle() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        vehiculeService.deleteVehicule(vehicule.getId());

        verify(vehiculeKilometrageRepository).detacherDesRelais(vehicule);
        verify(vehiculeRepository).delete(vehicule);
    }

    private VehiculeKilometrage savedKilometrage() {
        ArgumentCaptor<VehiculeKilometrage> captor = ArgumentCaptor.forClass(VehiculeKilometrage.class);
        verify(vehiculeKilometrageRepository).save(captor.capture());
        return captor.getValue();
    }

    private VehiculeUpdateRequest updateRequest(String relaiImmat) {
        VehiculeUpdateRequest request = new VehiculeUpdateRequest();
        request.setImmat(vehicule.getImmat());
        request.setBrand("Renault");
        request.setModel("Master");
        request.setRelaiImmat(relaiImmat);
        return request;
    }
}
