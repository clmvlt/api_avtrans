package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Commentaire interne sur une absence, réservé aux administrateurs : jamais exposé à un employé,
 * d'où l'absence volontaire de collection côté {@link Absence}.
 */
@Entity
@Table(name = "absence_comments",
        indexes = @Index(name = "idx_absence_comments_absence", columnList = "absence_uuid"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "absence_uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Absence absence;

    /** Null une fois l'auteur supprimé : le commentaire reste dans l'historique. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;
}
