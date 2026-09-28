package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiDTO;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiUpdateRequest;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
import bzh.stack.apiavtrans.mapper.VehiculeRelaiMapper;
import bzh.stack.apiavtrans.repository.VehiculeKilometrageRepository;
import bzh.stack.apiavtrans.repository.VehiculeRelaiRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeRelaiServiceTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final LocalDate DEBUT = LocalDate.of(2026, 9, 14);
    private static final LocalDate FIN = LocalDate.of(2026, 9, 18);

    @Mock
    private VehiculeRelaiRepository vehiculeRelaiRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private VehiculeKilometrageRepository vehiculeKilometrageRepository;
    @Spy
    private VehiculeRelaiMapper vehiculeRelaiMapper = new VehiculeRelaiMapper();

    @InjectMocks
    private VehiculeRelaiService vehiculeRelaiService;

    private Vehicule vehicule;

    @BeforeEach
    void setUp() {
        vehicule = new Vehicule();
        vehicule.setId(UUID.randomUUID());
        vehicule.setImmat("AB-123-CD");
    }

    // ── Déclaration ──

    @Test
    void should_clear_legacy_plate_and_attach_readings_when_creating_relay() {
        vehicule.setRelaiImmat("OLD-000");
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiRepository.findChevauchements(any(), any(), any(), any())).thenReturn(List.of());
        when(vehiculeRelaiRepository.save(any(VehiculeRelai.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeRelaiDTO dto = vehiculeRelaiService.createRelai(createRequest(" ef-456-gh ", DEBUT, FIN, 45000, null));

        assertThat(dto.getImmat()).isEqualTo("EF-456-GH");
        assertThat(dto.getVehiculeImmat()).isEqualTo("AB-123-CD");
        assertThat(vehicule.getRelaiImmat()).isNull();
        verify(vehiculeRepository).save(vehicule);
        // Relevés du 14 à 0 h au 19 à 0 h (fin incluse)
        verify(vehiculeKilometrageRepository).rattacherAuRelai(any(VehiculeRelai.class), eq(vehicule),
                eq(DEBUT.atStartOfDay(PARIS)), eq(FIN.plusDays(1).atStartOfDay(PARIS)));
    }

    @Test
    void should_attach_readings_until_far_future_when_relay_has_no_end() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiRepository.findChevauchements(any(), any(), any(), any())).thenReturn(List.of());
        when(vehiculeRelaiRepository.save(any(VehiculeRelai.class))).thenAnswer(inv -> inv.getArgument(0));

        vehiculeRelaiService.createRelai(createRequest("EF-456-GH", DEBUT, null, null, null));

        ArgumentCaptor<ZonedDateTime> fin = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(vehiculeKilometrageRepository).rattacherAuRelai(any(VehiculeRelai.class), eq(vehicule),
                eq(DEBUT.atStartOfDay(PARIS)), fin.capture());
        assertThat(fin.getValue().getYear()).isEqualTo(9999);
        verify(vehiculeRepository, never()).save(any());
    }

    @Test
    void should_reject_relay_when_period_overlaps_another_relay() {
        VehiculeRelai existant = relai("XY-999-ZZ", LocalDate.of(2026, 9, 10), null);
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeRelaiRepository.findChevauchements(eq(vehicule), any(), eq(DEBUT), eq(FIN)))
                .thenReturn(List.of(existant));

        assertThatThrownBy(() -> vehiculeRelaiService.createRelai(createRequest("EF-456-GH", DEBUT, FIN, null, null)))
                .hasMessage("Ce véhicule a déjà un relais sur cette période : XY-999-ZZ depuis le 10/09/2026, sans date de fin");
        verify(vehiculeRelaiRepository, never()).save(any());
    }

    @Test
    void should_reject_relay_when_plate_is_blank() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> vehiculeRelaiService.createRelai(createRequest("  ", DEBUT, FIN, null, null)))
                .hasMessage("L'immatriculation du véhicule relais est obligatoire");
    }

    @Test
    void should_reject_relay_when_end_is_before_start() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> vehiculeRelaiService.createRelai(createRequest("EF-456-GH", FIN, DEBUT, null, null)))
                .hasMessageContaining("date de fin");
    }

    @Test
    void should_reject_relay_when_return_km_is_below_start_km() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> vehiculeRelaiService.createRelai(createRequest("EF-456-GH", DEBUT, FIN, 45000, 44000)))
                .hasMessageContaining("kilométrage au retour");
    }

    @Test
    void should_reject_relay_when_start_date_is_missing() {
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> vehiculeRelaiService.createRelai(createRequest("EF-456-GH", null, FIN, null, null)))
                .hasMessage("La date de début du relais est obligatoire");
    }

    // ── Modification et suppression ──

    @Test
    void should_replace_all_fields_and_reattach_readings_when_updating_relay() {
        VehiculeRelai relai = relai("EF-456-GH", DEBUT, null);
        relai.setMotif("Garage");
        relai.setKmDebut(45000);
        when(vehiculeRelaiRepository.findById(relai.getId())).thenReturn(Optional.of(relai));
        when(vehiculeRelaiRepository.findChevauchements(eq(vehicule), eq(relai.getId()), any(), any())).thenReturn(List.of());
        when(vehiculeRelaiRepository.save(any(VehiculeRelai.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeRelaiUpdateRequest request = new VehiculeRelaiUpdateRequest();
        request.setImmat("EF-456-GH");
        request.setDateDebut(DEBUT);
        request.setDateFin(FIN);
        request.setKmFin(45850);

        VehiculeRelaiDTO dto = vehiculeRelaiService.updateRelai(relai.getId(), request);

        assertThat(dto.getDateFin()).isEqualTo(FIN);
        assertThat(dto.getKmFin()).isEqualTo(45850);
        // Remplacement complet : les champs absents sont effacés
        assertThat(dto.getMotif()).isNull();
        assertThat(dto.getKmDebut()).isNull();
        InOrder ordre = inOrder(vehiculeKilometrageRepository);
        ordre.verify(vehiculeKilometrageRepository).detacherDuRelai(relai);
        ordre.verify(vehiculeKilometrageRepository).rattacherAuRelai(relai, vehicule,
                DEBUT.atStartOfDay(PARIS), FIN.plusDays(1).atStartOfDay(PARIS));
    }

    @Test
    void should_detach_readings_when_deleting_relay() {
        VehiculeRelai relai = relai("EF-456-GH", DEBUT, FIN);
        vehicule.getRelais().add(relai);
        when(vehiculeRelaiRepository.findById(relai.getId())).thenReturn(Optional.of(relai));

        vehiculeRelaiService.deleteRelai(relai.getId());

        verify(vehiculeKilometrageRepository).detacherDuRelai(relai);
        verify(vehiculeRelaiRepository).delete(relai);
        assertThat(vehicule.getRelais()).isEmpty();
    }

    // ── DTO ──

    @Test
    void should_compute_distance_from_latest_reading_when_relay_is_running() {
        VehiculeRelai relai = relai("EF-456-GH", LocalDate.now(PARIS).minusDays(2), null);
        relai.setKmDebut(45000);
        VehiculeKilometrage releve = new VehiculeKilometrage();
        releve.setKm(45210);
        releve.setCreatedAt(ZonedDateTime.now(PARIS));
        when(vehiculeKilometrageRepository.findLatestByRelai(relai)).thenReturn(Optional.of(releve));
        when(vehiculeKilometrageRepository.countByRelai(relai)).thenReturn(3L);

        VehiculeRelaiDTO dto = vehiculeRelaiService.toDTO(relai);

        assertThat(dto.getStatut()).isEqualTo(VehiculeRelaiDTO.Statut.EN_COURS);
        assertThat(dto.getLatestKm()).isEqualTo(45210);
        assertThat(dto.getNbReleves()).isEqualTo(3L);
        assertThat(dto.getKmParcourus()).isEqualTo(210);
    }

    @Test
    void should_give_status_from_dates() {
        LocalDate today = LocalDate.of(2026, 9, 28);

        assertThat(VehiculeRelaiMapper.statutDe(relai("A", today.plusDays(1), null), today))
                .isEqualTo(VehiculeRelaiDTO.Statut.A_VENIR);
        assertThat(VehiculeRelaiMapper.statutDe(relai("B", today.minusDays(5), today), today))
                .isEqualTo(VehiculeRelaiDTO.Statut.EN_COURS);
        assertThat(VehiculeRelaiMapper.statutDe(relai("C", today.minusDays(5), today.minusDays(1)), today))
                .isEqualTo(VehiculeRelaiDTO.Statut.TERMINE);
    }

    // ── Helpers ──

    private VehiculeRelaiCreateRequest createRequest(String immat, LocalDate debut, LocalDate fin,
                                                     Integer kmDebut, Integer kmFin) {
        VehiculeRelaiCreateRequest request = new VehiculeRelaiCreateRequest();
        request.setVehiculeId(vehicule.getId());
        request.setImmat(immat);
        request.setDateDebut(debut);
        request.setDateFin(fin);
        request.setKmDebut(kmDebut);
        request.setKmFin(kmFin);
        return request;
    }

    private VehiculeRelai relai(String immat, LocalDate debut, LocalDate fin) {
        VehiculeRelai relai = new VehiculeRelai();
        relai.setId(UUID.randomUUID());
        relai.setVehicule(vehicule);
        relai.setImmat(immat);
        relai.setDateDebut(debut);
        relai.setDateFin(fin);
        return relai;
    }
}
