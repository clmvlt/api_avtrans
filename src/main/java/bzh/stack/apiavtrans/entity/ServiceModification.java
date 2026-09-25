package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Trace d'une action d'un administrateur sur un pointage (création, modification, suppression).
 * Le service est référencé par son UUID sans clé étrangère pour conserver l'historique après suppression.
 */
@Entity
@Table(name = "service_modifications", indexes = {
        @Index(name = "idx_service_modifications_service_uuid", columnList = "service_uuid"),
        @Index(name = "idx_service_modifications_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceModification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "service_uuid", nullable = false, updatable = false)
    private UUID serviceUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "modified_by_uuid")
    private User modifiedBy;

    @Column(name = "old_debut", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime oldDebut;

    @Column(name = "old_fin", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime oldFin;

    @Column(name = "old_is_break")
    private Boolean oldIsBreak;

    @Column(name = "new_debut", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime newDebut;

    @Column(name = "new_fin", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime newFin;

    @Column(name = "new_is_break")
    private Boolean newIsBreak;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    public enum Action {
        CREATE,
        UPDATE,
        DELETE
    }
}
