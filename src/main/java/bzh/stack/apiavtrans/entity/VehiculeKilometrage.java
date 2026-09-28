package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicule_kilometrages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeKilometrage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(name = "km", nullable = false)
    private Integer km;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    /**
     * Relais en cours à la date du relevé : le kilométrage est alors celui du véhicule relais, pas du
     * véhicule (null pour un relevé du véhicule lui-même).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relai_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VehiculeRelai relai;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        }
    }
}
