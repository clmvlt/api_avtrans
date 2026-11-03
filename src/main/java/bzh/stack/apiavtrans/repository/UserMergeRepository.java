package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Requêtes de la fusion de deux salariés : le profil supprimé cède toutes ses données au profil conservé.
 * Chaque association JPA vers {@link User} est listée ici une fois, transférée ou supprimée :
 * une nouvelle entité qui référence un salarié doit y être ajoutée (vérifié par {@code UserMergeRepositoryPostgresTest}).
 */
@Repository
public class UserMergeRepository {

    /** Association d'une entité vers un salarié (nom JPQL de l'entité, attribut). */
    public record Reference(String entite, String attribut) {
    }

    /** Associations transférées telles quelles au profil conservé. */
    public static final List<Reference> REFERENCES_TRANSFEREES = List.of(
            new Reference("Service", "user"),
            new Reference("Service", "modifiedBy"),
            new Reference("ServiceModification", "user"),
            new Reference("ServiceModification", "modifiedBy"),
            new Reference("Absence", "user"),
            new Reference("Absence", "validatedBy"),
            new Reference("AbsenceComment", "author"),
            new Reference("Acompte", "user"),
            new Reference("Acompte", "validatedBy"),
            new Reference("AppVersion", "createdBy"),
            new Reference("Carte", "user"),
            new Reference("Couchette", "user"),
            new Reference("Entretien", "mecanicien"),
            new Reference("Notification", "user"),
            new Reference("RapportVehicule", "user"),
            new Reference("Signature", "user"),
            new Reference("Todo", "createdBy"),
            new Reference("Todo", "completedBy"),
            new Reference("VehiculeAdjustInfo", "user"),
            new Reference("VehiculeKilometrage", "user"));

    /** Jetons propres au compte supprimé (vérification d'e-mail, mot de passe oublié) : supprimés, pas transférés. */
    public static final List<Reference> REFERENCES_SUPPRIMEES = List.of(
            new Reference("EmailVerification", "user"),
            new Reference("PasswordResetToken", "user"));

    /** Données d'un salarié comptées dans l'aperçu de la fusion. */
    public enum Donnee {
        POINTAGES("Pointages", "Service", "user", "e.isBreak = false"),
        ABSENCES("Absences", "Absence", "user", null),
        ACOMPTES("Acomptes", "Acompte", "user", null),
        SIGNATURES("Signatures", "Signature", "user", null),
        COUCHETTES("Couchettes", "Couchette", "user", null),
        RAPPORTS_VEHICULE("Rapports véhicule", "RapportVehicule", "user", null),
        KILOMETRAGES("Relevés kilométriques", "VehiculeKilometrage", "user", null),
        INFOS_AJUSTEMENT("Informations d'ajustement véhicule", "VehiculeAdjustInfo", "user", null),
        ENTRETIENS("Entretiens réalisés", "Entretien", "mecanicien", null),
        CARTES("Cartes", "Carte", "user", null),
        TACHES("Tâches créées", "Todo", "createdBy", null),
        NOTIFICATIONS("Notifications", "Notification", "user", null);

        private final String libelle;
        private final String entite;
        private final String attribut;
        private final String condition;

        Donnee(String libelle, String entite, String attribut, String condition) {
            this.libelle = libelle;
            this.entite = entite;
            this.attribut = attribut;
            this.condition = condition;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    public long compter(Donnee donnee, User user) {
        String jpql = "SELECT COUNT(e) FROM " + donnee.entite + " e WHERE e." + donnee.attribut + " = :user"
                + (donnee.condition != null ? " AND " + donnee.condition : "");
        return entityManager.createQuery(jpql, Long.class).setParameter("user", user).getSingleResult();
    }

    /** Services de travail du profil supprimé qui chevauchent un service de travail du profil conservé (en cours = jusqu'à maintenant). */
    public long compterServicesChevauchants(User supprime, User conserve, ZonedDateTime maintenant) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT s) FROM Service s, Service c " +
                        "WHERE s.user = :supprime AND c.user = :conserve " +
                        "AND s.isBreak = false AND c.isBreak = false " +
                        "AND s.debut < COALESCE(c.fin, :maintenant) AND c.debut < COALESCE(s.fin, :maintenant)", Long.class)
                .setParameter("supprime", supprime)
                .setParameter("conserve", conserve)
                .setParameter("maintenant", maintenant)
                .getSingleResult();
    }

    /** Absences non refusées du profil supprimé qui chevauchent une absence non refusée du profil conservé. */
    public long compterAbsencesChevauchantes(User supprime, User conserve) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT a) FROM Absence a, Absence c " +
                        "WHERE a.user = :supprime AND c.user = :conserve " +
                        "AND a.status <> :refusee AND c.status <> :refusee " +
                        "AND a.startDate <= c.endDate AND c.startDate <= a.endDate", Long.class)
                .setParameter("supprime", supprime)
                .setParameter("conserve", conserve)
                .setParameter("refusee", Absence.AbsenceStatus.REJECTED)
                .getSingleResult();
    }

    /** Couchettes du profil supprimé un jour où le profil conservé en a déjà une (contrainte unique salarié + date). */
    public long compterCouchettesEnDoublon(User supprime, User conserve) {
        return entityManager.createQuery(
                        "SELECT COUNT(s) FROM Couchette s WHERE s.user = :supprime " +
                        "AND EXISTS (SELECT c FROM Couchette c WHERE c.user = :conserve AND c.date = s.date)", Long.class)
                .setParameter("supprime", supprime)
                .setParameter("conserve", conserve)
                .getSingleResult();
    }

    /**
     * Transfère toutes les données du profil supprimé au profil conservé puis supprime le profil.
     * Les couchettes en doublon du profil supprimé sont abandonnées (celle du profil conservé reste).
     * Requêtes groupées : le contexte de persistance est vidé à la fin, les entités chargées avant sont périmées.
     */
    public void fusionner(User supprime, User conserve) {
        entityManager.flush();

        entityManager.createQuery(
                        "DELETE FROM Couchette s WHERE s.user = :supprime " +
                        "AND EXISTS (SELECT c FROM Couchette c WHERE c.user = :conserve AND c.date = s.date)")
                .setParameter("supprime", supprime)
                .setParameter("conserve", conserve)
                .executeUpdate();

        for (Reference reference : REFERENCES_TRANSFEREES) {
            entityManager.createQuery("UPDATE " + reference.entite() + " e SET e." + reference.attribut()
                            + " = :conserve WHERE e." + reference.attribut() + " = :supprime")
                    .setParameter("conserve", conserve)
                    .setParameter("supprime", supprime)
                    .executeUpdate();
        }

        for (Reference reference : REFERENCES_SUPPRIMEES) {
            entityManager.createQuery("DELETE FROM " + reference.entite() + " e WHERE e." + reference.attribut() + " = :supprime")
                    .setParameter("supprime", supprime)
                    .executeUpdate();
        }

        entityManager.createQuery("DELETE FROM User u WHERE u = :supprime")
                .setParameter("supprime", supprime)
                .executeUpdate();

        entityManager.clear();
    }
}
