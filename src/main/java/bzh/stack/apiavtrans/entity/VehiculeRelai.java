package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Véhicule relais qui remplace temporairement un véhicule de la flotte (garage, panne, sinistre...).
 * Les relevés kilométriques saisis sur le véhicule entre {@code dateDebut} et {@code dateFin}
 * (incluses) sont rattachés au relais. Un véhicule n'a jamais deux relais sur la même période.
 */
@Entity
@Table(name = "vehicule_relais")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeRelai {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vehicule_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vehicule vehicule;

    @Column(name = "immat", nullable = false, length = 20)
    private String immat;

    @Column(name = "marque", length = 255)
    private String marque;

    @Column(name = "modele", length = 255)
    private String modele;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    /** Dernier jour du relais (inclus) ; null tant que le véhicule n'est pas revenu. */
    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "km_debut")
    private Integer kmDebut;

    @Column(name = "km_fin")
    private Integer kmFin;

    @Column(name = "motif", length = 100)
    private String motif;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;
}
