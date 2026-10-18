package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "services", indexes = {
        @Index(name = "idx_services_service_parent", columnList = "service_parent_uuid"),
        @Index(name = "idx_services_user_debut", columnList = "user_uuid, debut")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "debut", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime debut;

    @Column(name = "fin", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime fin;

    @Column(name = "duree")
    private Long duree;

    @Column(name = "is_break", nullable = false)
    private Boolean isBreak = false;

    /**
     * Service de travail auquel appartient une pause (null pour un service de travail).
     * Une pause compte toujours avec son service, même si elle commence après minuit.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_parent_uuid", foreignKey = @ForeignKey(name = "fk_services_service_parent"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Service serviceParent;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude_end")
    private Double latitudeEnd;

    @Column(name = "longitude_end")
    private Double longitudeEnd;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @Column(name = "modified_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "modified_by_uuid")
    private User modifiedBy;
}
