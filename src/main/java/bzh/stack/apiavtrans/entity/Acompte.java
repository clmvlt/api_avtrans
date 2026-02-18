package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "acomptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acompte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @Column(name = "montant", nullable = false)
    private Double montant;

    @Column(name = "raison", length = 500)
    private String raison;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AcompteStatus status = AcompteStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "validated_by_uuid")
    private User validatedBy;

    @Column(name = "validated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime validatedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @Column(name = "paid_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime paidDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;

    public enum AcompteStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
