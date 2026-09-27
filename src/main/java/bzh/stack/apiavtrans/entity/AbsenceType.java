package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "absence_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "color", length = 7)
    private String color;

    /**
     * Mode de décompte des jours d'absence. Nullable en base (colonne ajoutée après coup,
     * renseignée par DataInitializer) : null = JOURS_OUVRABLES.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_decompte", length = 20)
    private ModeDecompte modeDecompte = ModeDecompte.JOURS_OUVRABLES;

    /**
     * L'absence crédite-t-elle des heures (congés payés, maladie…) ou non (sans solde) ?
     * Nullable en base (renseignée par DataInitializer) : null = true.
     */
    @Column(name = "compte_heures")
    private Boolean compteHeures = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    /**
     * Décompte des jours d'absence (règles françaises, convention des transports routiers).
     * Une semaine complète vaut toujours les heures hebdomadaires du contrat.
     */
    public enum ModeDecompte {
        /** Lundi au samedi (6 jours/semaine), dimanches et fériés exclus : congés payés. */
        JOURS_OUVRABLES(6),
        /** Lundi au vendredi (5 jours/semaine), week-ends et fériés exclus. */
        JOURS_OUVRES(5),
        /** Tous les jours (7 jours/semaine). */
        JOURS_CALENDAIRES(7);

        private final int joursParSemaine;

        ModeDecompte(int joursParSemaine) {
            this.joursParSemaine = joursParSemaine;
        }

        public int getJoursParSemaine() {
            return joursParSemaine;
        }
    }
}
