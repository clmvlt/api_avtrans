package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vehicules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "immat", nullable = false, length = 20)
    private String immat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "picture_path", length = 500)
    private String picturePath;

    @Column(name = "vin", length = 17)
    private String vin;

    @Column(name = "numero_carte_grise", length = 50)
    private String numeroCarteGrise;

    @Column(name = "date_mise_en_circulation")
    private LocalDate dateMiseEnCirculation;

    @Column(name = "type_carburant", length = 30)
    private String typeCarburant;

    @Column(name = "ptac")
    private Integer ptac;

    @Column(name = "numero_contrat_assurance", length = 100)
    private String numeroContratAssurance;

    @Column(name = "assureur", length = 100)
    private String assureur;

    @Column(name = "date_expiration_assurance")
    private LocalDate dateExpirationAssurance;

    @Column(name = "date_prochain_controle_technique")
    private LocalDate dateProchainControleTechnique;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehiculeAdjustInfo> adjustInfos = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehiculeFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehiculeTypeEntretien> typesEntretien = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehiculeKilometrage> kilometrages = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entretien> entretiens = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RapportVehicule> rapports = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehiculeEquipement> equipements = new ArrayList<>();
}
