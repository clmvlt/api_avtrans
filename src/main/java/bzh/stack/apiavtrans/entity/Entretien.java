package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entretiens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entretien {

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

    @ManyToOne
    @JoinColumn(name = "mecanicien_id", nullable = false)
    private User mecanicien;

    @Column(name = "date_entretien", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime dateEntretien;

    @Column(name = "kilometrage", nullable = false)
    private Integer kilometrage;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "cout")
    private Double cout;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "entretien", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntretienFile> files = new ArrayList<>();
}
