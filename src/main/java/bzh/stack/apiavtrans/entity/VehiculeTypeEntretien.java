package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicule_types_entretien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeTypeEntretien {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToOne
    @JoinColumn(name = "type_entretien_id", nullable = false)
    private TypeEntretien typeEntretien;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite_type", nullable = false)
    private PeriodiciteType periodiciteType;

    @Column(name = "periodicite_valeur", nullable = false)
    private Integer periodiciteValeur;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    public enum PeriodiciteType {
        KILOMETRAGE,
        TEMPOREL
    }
}
