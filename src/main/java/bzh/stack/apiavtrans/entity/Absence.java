package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "absence_type_uuid")
    private AbsenceType absenceType;

    @Column(name = "custom_type", length = 100)
    private String customType;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 20)
    private AbsencePeriod period = AbsencePeriod.FULL_DAY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AbsenceStatus status = AbsenceStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "validated_by_uuid")
    private User validatedBy;

    @Column(name = "validated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime validatedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;

    public enum AbsenceStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum AbsencePeriod {
        FULL_DAY,
        MORNING,
        AFTERNOON
    }
}
