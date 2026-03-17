package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "services")
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
}
