package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.entretien.AlerteEntretienDTO;
import bzh.stack.apiavtrans.dto.entretien.TypeEntretienDTO;
import bzh.stack.apiavtrans.dto.entretien.VehiculeProchainEntretienDTO;
import bzh.stack.apiavtrans.entity.Entretien;
import bzh.stack.apiavtrans.entity.TypeEntretien;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien.PeriodiciteType;
import bzh.stack.apiavtrans.mapper.EntretienMapper;
import bzh.stack.apiavtrans.mapper.TypeEntretienMapper;
import bzh.stack.apiavtrans.mapper.VehiculeMapper;
import bzh.stack.apiavtrans.repository.EntretienRepository;
import bzh.stack.apiavtrans.repository.VehiculeKilometrageRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import bzh.stack.apiavtrans.repository.VehiculeTypeEntretienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlerteServiceTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private VehiculeKilometrageRepository vehiculeKilometrageRepository;
    @Mock private VehiculeTypeEntretienRepository vehiculeTypeEntretienRepository;
    @Mock private EntretienRepository entretienRepository;
    @Mock private TypeEntretienMapper typeEntretienMapper;
    @Mock private EntretienMapper entretienMapper;
    @Mock private VehiculeMapper vehiculeMapper;

    @InjectMocks private AlerteService service;

    private final UUID vehiculeId = UUID.randomUUID();
    private final Vehicule vehicule = new Vehicule();
    private final List<VehiculeTypeEntretien> configurations = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(vehiculeRepository.findById(vehiculeId)).thenReturn(Optional.of(vehicule));
        when(vehiculeTypeEntretienRepository.findByVehiculeAndActifTrue(vehicule)).thenReturn(configurations);
        when(vehiculeKilometrageRepository.findLatestByVehicule(vehicule)).thenReturn(Optional.of(releve(100_000)));
        when(typeEntretienMapper.toDTO(any(TypeEntretien.class))).thenAnswer(invocation -> {
            TypeEntretienDTO dto = new TypeEntretienDTO();
            dto.setNom(((TypeEntretien) invocation.getArgument(0)).getNom());
            return dto;
        });
    }

    @Test
    void should_return_every_recurrence_when_vehicle_has_several_of_same_nature() {
        configurer("Vidange", PeriodiciteType.KILOMETRAGE, 30_000, entretienKm(90_000));
        configurer("Courroie", PeriodiciteType.KILOMETRAGE, 120_000, entretienKm(50_000));
        configurer("Pneus", PeriodiciteType.KILOMETRAGE, 40_000, entretienKm(95_000));
        configurer("Contrôle technique", PeriodiciteType.TEMPOREL, 365, entretienDate(joursDepuisAujourdhui(-300)));
        configurer("Tachygraphe", PeriodiciteType.TEMPOREL, 730, entretienDate(joursDepuisAujourdhui(-100)));

        VehiculeProchainEntretienDTO dto = service.getProchainEntretienForVehicule(vehiculeId);

        assertEquals(5, dto.getAlertes().size());
    }

    @Test
    void should_sort_alerts_by_urgency_when_natures_are_mixed() {
        configurer("Loin km", PeriodiciteType.KILOMETRAGE, 40_000, entretienKm(95_000));          // 35 000 km
        configurer("Retard km", PeriodiciteType.KILOMETRAGE, 30_000, entretienKm(60_000));        // -10 000 km
        configurer("Bientôt date", PeriodiciteType.TEMPOREL, 365, entretienDate(joursDepuisAujourdhui(-355))); // 10 j
        configurer("Retard date", PeriodiciteType.TEMPOREL, 365, entretienDate(joursDepuisAujourdhui(-368))); // -3 j

        List<String> ordre = service.getProchainEntretienForVehicule(vehiculeId).getAlertes().stream()
                .map(alerte -> alerte.getTypeEntretien().getNom())
                .toList();

        assertEquals(List.of("Retard km", "Retard date", "Bientôt date", "Loin km"), ordre);
    }

    @Test
    void should_keep_legacy_fields_as_nearest_of_each_nature_when_several_alerts_exist() {
        configurer("Vidange", PeriodiciteType.KILOMETRAGE, 30_000, entretienKm(90_000));
        configurer("Courroie", PeriodiciteType.KILOMETRAGE, 120_000, entretienKm(50_000));
        configurer("Contrôle technique", PeriodiciteType.TEMPOREL, 365, entretienDate(joursDepuisAujourdhui(-300)));
        configurer("Tachygraphe", PeriodiciteType.TEMPOREL, 730, entretienDate(joursDepuisAujourdhui(-100)));

        VehiculeProchainEntretienDTO dto = service.getProchainEntretienForVehicule(vehiculeId);

        assertEquals("Vidange", dto.getProchainEntretienKm().getTypeEntretien().getNom());
        assertEquals("Contrôle technique", dto.getProchainEntretienDate().getTypeEntretien().getNom());
    }

    @Test
    void should_return_empty_alerts_when_vehicle_has_no_recurrence() {
        VehiculeProchainEntretienDTO dto = service.getProchainEntretienForVehicule(vehiculeId);

        assertTrue(dto.getAlertes().isEmpty());
        assertNull(dto.getProchainEntretienKm());
        assertNull(dto.getProchainEntretienDate());
    }

    @Test
    void should_not_be_late_when_date_due_today() {
        configurer("Contrôle", PeriodiciteType.TEMPOREL, 30, entretienDate(joursDepuisAujourdhui(-30)));

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(0L, alerte.getJoursRestants());
        assertFalse(alerte.getEnRetard());
        assertEquals("Entretien à prévoir aujourd'hui", alerte.getMessage());
    }

    @Test
    void should_be_late_when_date_was_yesterday() {
        configurer("Contrôle", PeriodiciteType.TEMPOREL, 30, entretienDate(joursDepuisAujourdhui(-31)));

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(-1L, alerte.getJoursRestants());
        assertTrue(alerte.getEnRetard());
        assertEquals("URGENT : Entretien en retard de 1 jour", alerte.getMessage());
    }

    @Test
    void should_count_down_from_configuration_creation_when_date_recurrence_never_done() {
        VehiculeTypeEntretien config = configurer("Contrôle", PeriodiciteType.TEMPOREL, 100, null);
        config.setCreatedAt(joursDepuisAujourdhui(-40));

        assertEquals(60L, uniqueAlerte().getJoursRestants());
    }

    @Test
    void should_be_late_when_date_recurrence_never_done_and_period_elapsed() {
        VehiculeTypeEntretien config = configurer("Contrôle", PeriodiciteType.TEMPOREL, 100, null);
        config.setCreatedAt(joursDepuisAujourdhui(-140));

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(-40L, alerte.getJoursRestants());
        assertTrue(alerte.getEnRetard());
    }

    @Test
    void should_start_from_km_at_configuration_when_km_recurrence_never_done() {
        VehiculeTypeEntretien config = configurer("Vidange", PeriodiciteType.KILOMETRAGE, 30_000, null);
        when(vehiculeKilometrageRepository.findLatestByVehiculeAtOrBefore(eq(vehicule), eq(config.getCreatedAt())))
                .thenReturn(Optional.of(releve(90_000)));

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(120_000, alerte.getProchainKilometrage());
        assertEquals(20_000, alerte.getKmRestants());
        assertFalse(alerte.getEnRetard());
    }

    @Test
    void should_use_first_reading_when_no_km_known_at_configuration() {
        configurer("Vidange", PeriodiciteType.KILOMETRAGE, 30_000, null);
        when(vehiculeKilometrageRepository.findLatestByVehiculeAtOrBefore(eq(vehicule), any())).thenReturn(Optional.empty());
        when(vehiculeKilometrageRepository.findEarliestByVehicule(vehicule)).thenReturn(Optional.of(releve(95_000)));

        assertEquals(125_000, uniqueAlerte().getProchainKilometrage());
    }

    @Test
    void should_start_from_zero_when_vehicle_has_no_km_reading() {
        when(vehiculeKilometrageRepository.findLatestByVehicule(vehicule)).thenReturn(Optional.empty());
        when(vehiculeKilometrageRepository.findLatestByVehiculeAtOrBefore(eq(vehicule), any())).thenReturn(Optional.empty());
        when(vehiculeKilometrageRepository.findEarliestByVehicule(vehicule)).thenReturn(Optional.empty());
        configurer("Vidange", PeriodiciteType.KILOMETRAGE, 30_000, null);

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(30_000, alerte.getProchainKilometrage());
        assertEquals(30_000, alerte.getKmRestants());
    }

    @Test
    void should_be_late_when_km_reached() {
        configurer("Vidange", PeriodiciteType.KILOMETRAGE, 10_000, entretienKm(90_000));

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(0, alerte.getKmRestants());
        assertTrue(alerte.getEnRetard());
    }

    @Test
    void should_expose_periodicity_when_building_alert() {
        configurer("Vidange", PeriodiciteType.KILOMETRAGE, 30_000, entretienKm(90_000));

        AlerteEntretienDTO alerte = uniqueAlerte();

        assertEquals(PeriodiciteType.KILOMETRAGE, alerte.getPeriodiciteType());
        assertEquals(30_000, alerte.getPeriodiciteValeur());
    }

    @Test
    void should_fail_when_vehicle_unknown() {
        UUID inconnu = UUID.randomUUID();
        when(vehiculeRepository.findById(inconnu)).thenReturn(Optional.empty());

        RuntimeException erreur = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, () -> service.getProchainEntretienForVehicule(inconnu));

        assertTrue(erreur.getMessage().contains("Véhicule non trouvé"));
    }

    private AlerteEntretienDTO uniqueAlerte() {
        List<AlerteEntretienDTO> alertes = service.getProchainEntretienForVehicule(vehiculeId).getAlertes();
        assertEquals(1, alertes.size());
        return alertes.get(0);
    }

    private VehiculeTypeEntretien configurer(String nom, PeriodiciteType nature, int valeur, Entretien dernier) {
        TypeEntretien type = new TypeEntretien();
        type.setId(UUID.randomUUID());
        type.setNom(nom);

        VehiculeTypeEntretien config = new VehiculeTypeEntretien();
        config.setVehicule(vehicule);
        config.setTypeEntretien(type);
        config.setPeriodiciteType(nature);
        config.setPeriodiciteValeur(valeur);
        config.setCreatedAt(joursDepuisAujourdhui(-1));
        configurations.add(config);

        when(entretienRepository.findByVehiculeAndTypeEntretienOrderByDateEntretienDesc(vehicule, type.getId()))
                .thenReturn(dernier != null ? List.of(dernier) : List.of());
        return config;
    }

    private static Entretien entretienKm(int km) {
        Entretien entretien = new Entretien();
        entretien.setKilometrage(km);
        entretien.setDateEntretien(joursDepuisAujourdhui(-10));
        return entretien;
    }

    private static Entretien entretienDate(ZonedDateTime date) {
        Entretien entretien = new Entretien();
        entretien.setKilometrage(80_000);
        entretien.setDateEntretien(date);
        return entretien;
    }

    private static VehiculeKilometrage releve(int km) {
        VehiculeKilometrage releve = new VehiculeKilometrage();
        releve.setKm(km);
        return releve;
    }

    /** Midi à Paris, N jours après aujourd'hui : stable quelle que soit l'heure d'exécution du test. */
    private static ZonedDateTime joursDepuisAujourdhui(int jours) {
        return ZonedDateTime.now(PARIS).toLocalDate().plusDays(jours).atTime(12, 0).atZone(PARIS);
    }
}
