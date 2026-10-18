package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Requêtes du pointage sur un vrai PostgreSQL : Flyway (base vide) puis schéma Hibernate, comme en production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServiceRepositoryPostgresTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final EmbeddedPostgres POSTGRES = demarrer();

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ServiceRepository serviceRepository;

    private User chauffeur;
    private User collegue;
    private Service nuit;
    private Service pauseApresMinuit;
    private Service enCours;
    private Service pauseEnCours;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @AfterAll
    static void arreter() throws IOException {
        POSTGRES.close();
    }

    @BeforeEach
    void setUp() {
        chauffeur = user("chauffeur@avtrans.fr");
        collegue = user("collegue@avtrans.fr");

        nuit = service(chauffeur, at(12, 21), at(13, 5), null);
        pauseApresMinuit = service(chauffeur, at(13, 1), at(13, 2), nuit);
        service(collegue, at(12, 20), at(13, 6), null);
        enCours = service(chauffeur, at(16, 21), null, null);
        pauseEnCours = service(chauffeur, at(17, 1), null, enCours);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void should_return_only_work_services_started_in_window() {
        List<Service> le12 = serviceRepository.findServicesTravail(chauffeur, day(12), day(13));
        List<Service> le13 = serviceRepository.findServicesTravail(chauffeur, day(13), day(14));

        assertThat(le12).extracting(Service::getUuid).containsExactly(nuit.getUuid());
        assertThat(le13).isEmpty();
        assertThat(serviceRepository.existsServiceTravail(chauffeur, day(12), day(13))).isTrue();
        assertThat(serviceRepository.existsServiceTravail(chauffeur, day(13), day(14))).isFalse();
    }

    @Test
    void should_load_breaks_of_several_services_in_one_query() {
        List<Service> services = serviceRepository.findServicesTravail(chauffeur, day(1), day(31));

        List<Service> pauses = serviceRepository.findPausesDe(services);

        assertThat(pauses).extracting(Service::getUuid)
                .containsExactly(pauseApresMinuit.getUuid(), pauseEnCours.getUuid());
        assertThat(pauses.get(0).getServiceParent().getUuid()).isEqualTo(nuit.getUuid());
    }

    @Test
    void should_return_running_work_service_and_not_running_break() {
        assertThat(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(chauffeur))
                .map(Service::getUuid).contains(enCours.getUuid());
        Service actif = entityManager.find(Service.class, enCours.getUuid());
        assertThat(serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(actif))
                .map(Service::getUuid).contains(pauseEnCours.getUuid());
    }

    @Test
    void should_find_work_service_of_the_user_covering_an_instant() {
        assertThat(serviceRepository.findServicesTravailContenant(chauffeur, at(13, 3)))
                .extracting(Service::getUuid).containsExactly(nuit.getUuid());
        assertThat(serviceRepository.findServicesTravailContenant(chauffeur, at(17, 4)))
                .extracting(Service::getUuid).containsExactly(enCours.getUuid());
        assertThat(serviceRepository.findServicesTravailContenant(chauffeur, at(14, 12))).isEmpty();
    }

    @Test
    void should_find_latest_work_service_touching_a_day_when_night_service_crosses_midnight() {
        // Le 13/10 : le service de nuit du 12/10 (fini à 5 h) touche la journée ; le 14/10 rien
        assertThat(serviceRepository.findServicesTravailChevauchant(chauffeur, day(13), day(14), PageRequest.of(0, 1)))
                .extracting(Service::getUuid).containsExactly(nuit.getUuid());
        assertThat(serviceRepository.findServicesTravailChevauchant(chauffeur, day(14), day(15), PageRequest.of(0, 1)))
                .isEmpty();
        // Le 17/10 : le service en cours depuis le 16/10 touche la journée
        assertThat(serviceRepository.findServicesTravailChevauchant(chauffeur, day(17), day(18), PageRequest.of(0, 1)))
                .extracting(Service::getUuid).containsExactly(enCours.getUuid());
        // Le 12/10 : le service du collègue n'est jamais renvoyé
        assertThat(serviceRepository.findServicesTravailChevauchant(chauffeur, day(12), day(13), PageRequest.of(0, 5)))
                .extracting(Service::getUuid).containsExactly(nuit.getUuid());
    }

    @Test
    void should_filter_history_with_optional_parameters_on_postgres() {
        var page = serviceRepository.findByUserWithFilters(chauffeur, false, day(12), null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "debut")));
        var tout = serviceRepository.findByUserWithFilters(chauffeur, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Service::getUuid).containsExactly(enCours.getUuid(), nuit.getUuid());
        assertThat(tout.getTotalElements()).isEqualTo(4);
    }

    @Test
    void should_prevent_deleting_a_service_that_still_has_breaks() {
        assertThatThrownBy(() -> {
            entityManager.createNativeQuery("DELETE FROM services WHERE uuid = :uuid")
                    .setParameter("uuid", nuit.getUuid())
                    .executeUpdate();
        }).hasStackTraceContaining("fk_services_service_parent");
    }

    @Test
    void should_delete_breaks_then_services_when_deleting_user_pointages() {
        serviceRepository.deletePausesByUser(chauffeur);
        serviceRepository.deleteServicesByUser(chauffeur);

        assertThat(serviceRepository.findServicesTravail(chauffeur, day(1), day(31))).isEmpty();
        assertThat(serviceRepository.findServicesTravail(collegue, day(1), day(31))).hasSize(1);
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("x");
        entityManager.persist(user);
        return user;
    }

    private Service service(User user, ZonedDateTime debut, ZonedDateTime fin, Service parent) {
        Service service = new Service();
        service.setUser(user);
        service.setDebut(debut);
        service.setFin(fin);
        service.setIsBreak(parent != null);
        service.setServiceParent(parent);
        entityManager.persist(service);
        return service;
    }

    private static ZonedDateTime at(int jourOctobre, int heure) {
        return ZonedDateTime.of(2026, 10, jourOctobre, heure, 0, 0, 0, PARIS);
    }

    private static ZonedDateTime day(int jourOctobre) {
        return at(jourOctobre, 0);
    }

    private static EmbeddedPostgres demarrer() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
