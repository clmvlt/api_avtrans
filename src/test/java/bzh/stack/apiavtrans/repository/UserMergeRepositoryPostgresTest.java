package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceComment;
import bzh.stack.apiavtrans.entity.Acompte;
import bzh.stack.apiavtrans.entity.Couchette;
import bzh.stack.apiavtrans.entity.EmailVerification;
import bzh.stack.apiavtrans.entity.Notification;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.Signature;
import bzh.stack.apiavtrans.entity.Todo;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.UserMergeRepository.Donnee;
import bzh.stack.apiavtrans.repository.UserMergeRepository.Reference;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fusion de deux salariés sur un vrai PostgreSQL (requêtes groupées, sous-requêtes, contraintes) :
 * Flyway (base vide) puis schéma Hibernate, comme en production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserMergeRepository.class)
class UserMergeRepositoryPostgresTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final EmbeddedPostgres POSTGRES = demarrer();

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserMergeRepository userMergeRepository;

    private User conserve;
    private User supprime;

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
        conserve = user("conserve@avtrans.fr");
        supprime = user("doublon@avtrans.fr");
    }

    @Test
    void should_list_every_user_association_when_entities_reference_a_user() {
        List<Reference> associations = new ArrayList<>();
        for (EntityType<?> entite : entityManager.getMetamodel().getEntities()) {
            for (Attribute<?, ?> attribut : entite.getAttributes()) {
                if (attribut.getJavaType() == User.class) {
                    associations.add(new Reference(entite.getName(), attribut.getName()));
                }
            }
        }

        List<Reference> prevues = new ArrayList<>(UserMergeRepository.REFERENCES_TRANSFEREES);
        prevues.addAll(UserMergeRepository.REFERENCES_SUPPRIMEES);
        assertThat(associations).containsExactlyInAnyOrderElementsOf(prevues);
    }

    @Test
    void should_transfer_all_data_and_delete_source_when_merging() {
        Service service = service(supprime, at(1, 8), at(1, 17));
        Service pause = service(supprime, at(1, 12), at(1, 13));
        pause.setIsBreak(true);
        pause.setServiceParent(service);
        Service modifie = service(conserve, at(2, 8), at(2, 17));
        modifie.setModifiedBy(supprime);
        Absence absence = absence(supprime, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6));
        commentaire(absence(conserve, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 20)), supprime);
        Acompte acompte = new Acompte();
        acompte.setUser(conserve);
        acompte.setMontant(100.0);
        acompte.setValidatedBy(supprime);
        entityManager.persist(acompte);
        Signature signature = new Signature();
        signature.setUser(supprime);
        signature.setSignatureBase64("data");
        signature.setDate(at(3, 9));
        signature.setHeuresSignees(151.67);
        entityManager.persist(signature);
        Todo todo = new Todo();
        todo.setTitle("Vérifier les pneus");
        todo.setCreatedBy(supprime);
        todo.setCompletedBy(supprime);
        entityManager.persist(todo);
        Notification notification = new Notification();
        notification.setUser(supprime);
        notification.setTitle("Absence validée");
        notification.setDescription("Votre absence a été validée");
        entityManager.persist(notification);
        EmailVerification verification = new EmailVerification();
        verification.setUser(supprime);
        verification.setEmail(supprime.getEmail());
        verification.setToken(UUID.randomUUID().toString());
        verification.setVerificationDateTime(at(1, 8));
        entityManager.persist(verification);
        entityManager.flush();

        userMergeRepository.fusionner(supprime, conserve);

        assertThat(entityManager.find(User.class, supprime.getUuid())).isNull();
        assertThat(entityManager.find(Service.class, service.getUuid()).getUser().getUuid()).isEqualTo(conserve.getUuid());
        Service pauseFusionnee = entityManager.find(Service.class, pause.getUuid());
        assertThat(pauseFusionnee.getUser().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(pauseFusionnee.getServiceParent().getUuid()).isEqualTo(service.getUuid());
        assertThat(entityManager.find(Service.class, modifie.getUuid()).getModifiedBy().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(entityManager.find(Absence.class, absence.getUuid()).getUser().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(entityManager.createQuery("SELECT c.author.uuid FROM AbsenceComment c", UUID.class).getResultList())
                .containsExactly(conserve.getUuid());
        assertThat(entityManager.find(Acompte.class, acompte.getUuid()).getValidatedBy().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(entityManager.find(Signature.class, signature.getUuid()).getUser().getUuid()).isEqualTo(conserve.getUuid());
        Todo todoFusionne = entityManager.find(Todo.class, todo.getUuid());
        assertThat(todoFusionne.getCreatedBy().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(todoFusionne.getCompletedBy().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(entityManager.find(Notification.class, notification.getUuid()).getUser().getUuid()).isEqualTo(conserve.getUuid());
        assertThat(entityManager.find(EmailVerification.class, verification.getUuid())).isNull();
    }

    @Test
    void should_keep_target_couchette_when_both_profiles_have_one_the_same_day() {
        Couchette doublonConserve = couchette(conserve, LocalDate.of(2026, 10, 10));
        Couchette doublonSupprime = couchette(supprime, LocalDate.of(2026, 10, 10));
        Couchette autreJour = couchette(supprime, LocalDate.of(2026, 10, 11));
        entityManager.flush();

        assertThat(userMergeRepository.compterCouchettesEnDoublon(supprime, conserve)).isEqualTo(1);

        userMergeRepository.fusionner(supprime, conserve);

        assertThat(entityManager.find(Couchette.class, doublonConserve.getUuid())).isNotNull();
        assertThat(entityManager.find(Couchette.class, doublonSupprime.getUuid())).isNull();
        assertThat(entityManager.find(Couchette.class, autreJour.getUuid()).getUser().getUuid()).isEqualTo(conserve.getUuid());
    }

    @Test
    void should_count_overlapping_work_services_only_when_merging() {
        service(conserve, at(1, 8), at(1, 12));
        service(conserve, at(1, 14), null);
        service(supprime, at(1, 11), at(1, 15)); // chevauche les deux services du conservé : compté une fois
        service(supprime, at(1, 12), at(1, 14)); // entre les deux : ne chevauche pas
        Service pause = service(supprime, at(1, 9), at(1, 10));
        pause.setIsBreak(true);
        entityManager.flush();

        assertThat(userMergeRepository.compterServicesChevauchants(supprime, conserve, at(1, 18))).isEqualTo(1);
    }

    @Test
    void should_return_zero_when_profiles_have_no_data() {
        assertThat(userMergeRepository.compterServicesChevauchants(supprime, conserve, at(1, 18))).isZero();
        assertThat(userMergeRepository.compterAbsencesChevauchantes(supprime, conserve)).isZero();
        assertThat(userMergeRepository.compterCouchettesEnDoublon(supprime, conserve)).isZero();
        for (Donnee donnee : Donnee.values()) {
            assertThat(userMergeRepository.compter(donnee, supprime)).as(donnee.name()).isZero();
        }
    }

    @Test
    void should_count_overlapping_absences_except_rejected_ones() {
        absence(conserve, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));
        absence(supprime, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 12)); // touche le dernier jour
        absence(supprime, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12)); // après
        Absence refusee = absence(supprime, LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 6));
        refusee.setStatus(Absence.AbsenceStatus.REJECTED);
        entityManager.flush();

        assertThat(userMergeRepository.compterAbsencesChevauchantes(supprime, conserve)).isEqualTo(1);
    }

    @Test
    void should_count_work_services_without_breaks_when_previewing() {
        Service service = service(supprime, at(1, 8), at(1, 17));
        Service pause = service(supprime, at(1, 12), at(1, 13));
        pause.setIsBreak(true);
        pause.setServiceParent(service);
        entityManager.flush();

        assertThat(userMergeRepository.compter(Donnee.POINTAGES, supprime)).isEqualTo(1);
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("x");
        entityManager.persist(user);
        return user;
    }

    private Service service(User user, ZonedDateTime debut, ZonedDateTime fin) {
        Service service = new Service();
        service.setUser(user);
        service.setDebut(debut);
        service.setFin(fin);
        entityManager.persist(service);
        return service;
    }

    private Absence absence(User user, LocalDate debut, LocalDate fin) {
        Absence absence = new Absence();
        absence.setUser(user);
        absence.setStartDate(debut);
        absence.setEndDate(fin);
        entityManager.persist(absence);
        return absence;
    }

    private void commentaire(Absence absence, User author) {
        AbsenceComment comment = new AbsenceComment();
        comment.setAbsence(absence);
        comment.setAuthor(author);
        comment.setContent("Vu avec le salarié");
        entityManager.persist(comment);
    }

    private Couchette couchette(User user, LocalDate date) {
        Couchette couchette = new Couchette();
        couchette.setUser(user);
        couchette.setDate(date);
        entityManager.persist(couchette);
        return couchette;
    }

    private static ZonedDateTime at(int jour, int heure) {
        return ZonedDateTime.of(2026, 10, jour, heure, 0, 0, 0, PARIS);
    }

    private static EmbeddedPostgres demarrer() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
