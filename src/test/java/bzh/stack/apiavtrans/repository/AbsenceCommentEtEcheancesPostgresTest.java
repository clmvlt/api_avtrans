package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceComment;
import bzh.stack.apiavtrans.entity.Entretien;
import bzh.stack.apiavtrans.entity.TypeEntretien;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requêtes des commentaires d'absence et des échéances d'entretien sur un vrai PostgreSQL :
 * Flyway (base vide) puis schéma Hibernate, comme en production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AbsenceCommentEtEcheancesPostgresTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final EmbeddedPostgres POSTGRES = demarrer();

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private AbsenceCommentRepository absenceCommentRepository;
    @Autowired
    private AbsenceRepository absenceRepository;
    @Autowired
    private VehiculeKilometrageRepository vehiculeKilometrageRepository;
    @Autowired
    private EntretienRepository entretienRepository;

    private User employe;
    private User admin;
    private Absence avecCommentaires;
    private Absence sansCommentaire;

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
        employe = user("employe@avtrans.fr");
        admin = user("admin@avtrans.fr");
        avecCommentaires = absence(employe);
        sansCommentaire = absence(employe);
    }

    @Test
    void should_return_comments_in_order_with_their_author() throws InterruptedException {
        AbsenceComment premier = commentaire(avecCommentaires, admin, "Premier");
        Thread.sleep(5); // created_at distincts : à égalité, l'ordre suit l'UUID
        AbsenceComment second = commentaire(avecCommentaires, admin, "Second");
        commentaire(sansCommentaire, admin, "Autre absence");
        entityManager.flush();
        entityManager.clear();

        List<AbsenceComment> comments = absenceCommentRepository.findByAbsenceUuidInOrder(avecCommentaires.getUuid());

        assertThat(comments).extracting(AbsenceComment::getUuid).containsExactly(premier.getUuid(), second.getUuid());
        assertThat(comments.get(0).getAuthor().getEmail()).isEqualTo("admin@avtrans.fr");
    }

    @Test
    void should_keep_comment_without_author_when_author_removed() {
        AbsenceComment comment = commentaire(avecCommentaires, admin, "Reste dans l'historique");
        entityManager.flush();

        absenceCommentRepository.setAuthorToNull(admin);
        entityManager.flush();
        entityManager.clear();

        List<AbsenceComment> comments = absenceCommentRepository.findByAbsenceUuidInOrder(avecCommentaires.getUuid());

        assertThat(comments).extracting(AbsenceComment::getUuid).containsExactly(comment.getUuid());
        assertThat(comments.get(0).getAuthor()).isNull();
    }

    @Test
    void should_count_comments_per_absence_in_one_query() {
        commentaire(avecCommentaires, admin, "Un");
        commentaire(avecCommentaires, admin, "Deux");
        entityManager.flush();

        List<AbsenceCommentRepository.AbsenceCommentCount> counts = absenceCommentRepository
                .countByAbsenceUuids(List.of(avecCommentaires.getUuid(), sansCommentaire.getUuid()));

        assertThat(counts).hasSize(1);
        assertThat(counts.get(0).getAbsenceUuid()).isEqualTo(avecCommentaires.getUuid());
        assertThat(counts.get(0).getCommentsCount()).isEqualTo(2L);
    }

    @Test
    void should_return_only_latest_comment_of_each_absence_in_one_query() throws InterruptedException {
        commentaire(avecCommentaires, admin, "Ancien");
        Thread.sleep(5); // created_at distincts
        AbsenceComment dernier = commentaire(avecCommentaires, admin, "Dernier");
        entityManager.flush();
        entityManager.clear();

        List<AbsenceComment> latest = absenceCommentRepository
                .findLatestByAbsenceUuids(List.of(avecCommentaires.getUuid(), sansCommentaire.getUuid()));

        assertThat(latest).extracting(AbsenceComment::getUuid).containsExactly(dernier.getUuid());
        assertThat(latest.get(0).getAuthor().getEmail()).isEqualTo("admin@avtrans.fr");
    }

    @Test
    void should_allow_absence_deletion_when_comments_removed_first() {
        commentaire(avecCommentaires, admin, "À supprimer");
        entityManager.flush();
        entityManager.clear();
        UUID uuid = avecCommentaires.getUuid();

        absenceCommentRepository.deleteByAbsenceUuid(uuid);
        absenceRepository.deleteById(uuid);
        entityManager.flush();
        entityManager.clear();

        assertThat(absenceRepository.findById(uuid)).isEmpty();
        assertThat(absenceCommentRepository.findByAbsenceUuidInOrder(uuid)).isEmpty();
    }

    @Test
    void should_allow_user_deletion_when_comments_of_his_absences_removed_first() {
        commentaire(avecCommentaires, admin, "Sur une absence de l'employé");
        commentaire(absence(admin), employe, "Écrit par l'employé devenu admin puis supprimé");
        entityManager.flush();
        entityManager.clear();
        User supprime = entityManager.find(User.class, employe.getUuid());

        absenceCommentRepository.setAuthorToNull(supprime);
        absenceCommentRepository.deleteByAbsenceUser(supprime);
        absenceRepository.deleteByUser(supprime);
        entityManager.flush();
        entityManager.remove(supprime);
        entityManager.flush();
        entityManager.clear();

        assertThat(absenceCommentRepository.findAll())
                .extracting(AbsenceComment::getContent)
                .containsExactly("Écrit par l'employé devenu admin puis supprimé");
    }

    @Test
    void should_find_km_known_at_date_ignoring_relay_readings() {
        Vehicule vehicule = vehicule("AA-111-AA");
        Vehicule autre = vehicule("BB-222-BB");
        releve(vehicule, 90_000, at(1));
        releve(vehicule, 95_000, at(10));
        releve(vehicule, 110_000, at(20));
        releve(autre, 1, at(5));
        entityManager.flush();

        assertThat(vehiculeKilometrageRepository.findLatestByVehiculeAtOrBefore(vehicule, at(15)))
                .get().extracting(VehiculeKilometrage::getKm).isEqualTo(95_000);
        assertThat(vehiculeKilometrageRepository.findLatestByVehiculeAtOrBefore(vehicule, at(10)))
                .get().extracting(VehiculeKilometrage::getKm).isEqualTo(95_000);
        assertThat(vehiculeKilometrageRepository.findLatestByVehiculeAtOrBefore(vehicule, at(0))).isEmpty();
        assertThat(vehiculeKilometrageRepository.findEarliestByVehicule(vehicule))
                .get().extracting(VehiculeKilometrage::getKm).isEqualTo(90_000);
    }

    @Test
    void should_take_last_created_maintenance_when_same_date() {
        Vehicule vehicule = vehicule("CC-333-CC");
        TypeEntretien type = new TypeEntretien();
        type.setNom("Vidange");
        entityManager.persist(type);
        ZonedDateTime date = at(12);
        entretien(vehicule, type, date, 100_000);
        Entretien dernier = entretien(vehicule, type, date, 120_000);
        entretien(vehicule, type, at(1), 90_000);
        entityManager.flush();
        entityManager.clear();
        // created_at n'est pas modifiable par l'entité : on force celui du dernier enregistré
        entityManager.createNativeQuery("UPDATE entretiens SET created_at = :date WHERE id = :id")
                .setParameter("date", ZonedDateTime.now(PARIS).plusDays(1).toOffsetDateTime())
                .setParameter("id", dernier.getId())
                .executeUpdate();

        List<Entretien> entretiens = entretienRepository
                .findByVehiculeAndTypeEntretienOrderByDateEntretienDesc(entityManager.find(Vehicule.class, vehicule.getId()), type.getId());

        assertThat(entretiens).extracting(Entretien::getKilometrage).containsExactly(120_000, 100_000, 90_000);
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("x");
        entityManager.persist(user);
        return user;
    }

    private Absence absence(User user) {
        Absence absence = new Absence();
        absence.setUser(user);
        absence.setStartDate(LocalDate.of(2026, 11, 3));
        absence.setEndDate(LocalDate.of(2026, 11, 5));
        entityManager.persist(absence);
        return absence;
    }

    private AbsenceComment commentaire(Absence absence, User author, String content) {
        AbsenceComment comment = new AbsenceComment();
        comment.setAbsence(absence);
        comment.setAuthor(author);
        comment.setContent(content);
        entityManager.persist(comment);
        // created_at est généré à l'écriture en base
        entityManager.flush();
        return comment;
    }

    private Vehicule vehicule(String immat) {
        Vehicule vehicule = new Vehicule();
        vehicule.setImmat(immat);
        entityManager.persist(vehicule);
        return vehicule;
    }

    private void releve(Vehicule vehicule, int km, ZonedDateTime date) {
        VehiculeKilometrage releve = new VehiculeKilometrage();
        releve.setVehicule(vehicule);
        releve.setKm(km);
        releve.setCreatedAt(date);
        entityManager.persist(releve);
    }

    private Entretien entretien(Vehicule vehicule, TypeEntretien type, ZonedDateTime date, int km) {
        Entretien entretien = new Entretien();
        entretien.setVehicule(vehicule);
        entretien.setTypeEntretien(type);
        entretien.setMecanicien(admin);
        entretien.setDateEntretien(date);
        entretien.setKilometrage(km);
        entityManager.persist(entretien);
        return entretien;
    }

    private static ZonedDateTime at(int jour) {
        return ZonedDateTime.of(2026, 10, 1, 12, 0, 0, 0, PARIS).plusDays(jour);
    }

    private static EmbeddedPostgres demarrer() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
