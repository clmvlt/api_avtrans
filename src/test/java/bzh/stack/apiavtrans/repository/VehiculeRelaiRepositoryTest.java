package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requêtes des véhicules relais et du rattachement des relevés kilométriques (base H2 en mémoire).
 */
@DataJpaTest
class VehiculeRelaiRepositoryTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final LocalDate DEBUT = LocalDate.of(2026, 9, 14);
    private static final LocalDate FIN = LocalDate.of(2026, 9, 18);
    private static final LocalDate SANS_FIN = LocalDate.of(9999, 12, 31);

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private VehiculeRelaiRepository vehiculeRelaiRepository;
    @Autowired
    private VehiculeKilometrageRepository vehiculeKilometrageRepository;

    private Vehicule vehicule;
    private VehiculeRelai relai;

    @BeforeEach
    void setUp() {
        vehicule = new Vehicule();
        vehicule.setImmat("AB-123-CD");
        entityManager.persist(vehicule);

        relai = new VehiculeRelai();
        relai.setVehicule(vehicule);
        relai.setImmat("EF-456-GH");
        relai.setDateDebut(DEBUT);
        relai.setDateFin(FIN);
        entityManager.persist(relai);
        entityManager.flush();
    }

    @Test
    void should_find_running_relay_on_first_and_last_day_only() {
        assertThat(vehiculeRelaiRepository.findActifLe(vehicule, DEBUT)).contains(relai);
        assertThat(vehiculeRelaiRepository.findActifLe(vehicule, FIN)).contains(relai);
        assertThat(vehiculeRelaiRepository.findActifLe(vehicule, DEBUT.minusDays(1))).isEmpty();
        assertThat(vehiculeRelaiRepository.findActifLe(vehicule, FIN.plusDays(1))).isEmpty();
        assertThat(vehiculeRelaiRepository.findAllActifsLe(FIN)).containsExactly(relai);
    }

    @Test
    void should_find_running_relay_when_it_has_no_end() {
        relai.setDateFin(null);
        entityManager.flush();

        assertThat(vehiculeRelaiRepository.findActifLe(vehicule, FIN.plusYears(1))).contains(relai);
    }

    @Test
    void should_detect_overlap_except_with_itself() {
        assertThat(vehiculeRelaiRepository.findChevauchements(vehicule, new UUID(0L, 0L), FIN, SANS_FIN))
                .containsExactly(relai);
        assertThat(vehiculeRelaiRepository.findChevauchements(vehicule, new UUID(0L, 0L), FIN.plusDays(1), SANS_FIN))
                .isEmpty();
        assertThat(vehiculeRelaiRepository.findChevauchements(vehicule, relai.getId(), DEBUT, FIN)).isEmpty();
        assertThat(vehiculeRelaiRepository.existsByVehicule(vehicule)).isTrue();
    }

    @Test
    void should_attach_readings_of_the_period_and_exclude_them_from_vehicle_km() {
        VehiculeKilometrage avant = releve(125000, DEBUT.minusDays(1).atTime(18, 0).atZone(PARIS));
        VehiculeKilometrage pendant = releve(45210, DEBUT.atTime(7, 0).atZone(PARIS));
        VehiculeKilometrage dernierJour = releve(45600, FIN.atTime(23, 30).atZone(PARIS));
        VehiculeKilometrage apres = releve(125010, FIN.plusDays(1).atTime(6, 0).atZone(PARIS));

        int rattaches = vehiculeKilometrageRepository.rattacherAuRelai(relai, vehicule,
                DEBUT.atStartOfDay(PARIS), FIN.plusDays(1).atStartOfDay(PARIS));
        entityManager.clear();

        assertThat(rattaches).isEqualTo(2);
        assertThat(vehiculeKilometrageRepository.countByRelai(relai)).isEqualTo(2);
        assertThat(vehiculeKilometrageRepository.findLatestByRelai(relai))
                .hasValueSatisfying(k -> assertThat(k.getId()).isEqualTo(dernierJour.getId()));
        // Le kilométrage du véhicule ignore les relevés du relais
        assertThat(vehiculeKilometrageRepository.findLatestByVehicule(vehicule))
                .hasValueSatisfying(k -> assertThat(k.getId()).isEqualTo(apres.getId()));
        assertThat(entityManager.find(VehiculeKilometrage.class, avant.getId()).getRelai()).isNull();
        assertThat(entityManager.find(VehiculeKilometrage.class, pendant.getId()).getRelai().getId())
                .isEqualTo(relai.getId());
    }

    @Test
    void should_give_readings_back_to_vehicle_when_detaching() {
        releve(45210, DEBUT.atTime(7, 0).atZone(PARIS));
        vehiculeKilometrageRepository.rattacherAuRelai(relai, vehicule,
                DEBUT.atStartOfDay(PARIS), FIN.plusDays(1).atStartOfDay(PARIS));

        int detaches = vehiculeKilometrageRepository.detacherDuRelai(relai);
        entityManager.clear();

        assertThat(detaches).isEqualTo(1);
        assertThat(vehiculeKilometrageRepository.countByRelai(relai)).isZero();
        assertThat(vehiculeKilometrageRepository.findLatestByVehicule(vehicule))
                .hasValueSatisfying(k -> assertThat(k.getKm()).isEqualTo(45210));
        assertThat(vehiculeKilometrageRepository.detacherDesRelais(vehicule)).isZero();
    }

    private VehiculeKilometrage releve(int km, ZonedDateTime createdAt) {
        VehiculeKilometrage kilometrage = new VehiculeKilometrage();
        kilometrage.setVehicule(vehicule);
        kilometrage.setKm(km);
        kilometrage.setCreatedAt(createdAt);
        entityManager.persist(kilometrage);
        entityManager.flush();
        return kilometrage;
    }
}
