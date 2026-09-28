package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Véhicule relais qui remplace temporairement un véhicule de la flotte")
public class VehiculeRelaiDTO {

    /** Statut d'un relais à la date du jour. */
    public enum Statut {
        A_VENIR,
        EN_COURS,
        TERMINE
    }

    @Schema(description = "Identifiant unique du relais", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Identifiant du véhicule remplacé", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID vehiculeId;

    @Schema(description = "Immatriculation du véhicule remplacé", example = "AB-123-CD")
    private String vehiculeImmat;

    @Schema(description = "Immatriculation du véhicule relais", example = "EF-456-GH")
    private String immat;

    @Schema(description = "Marque du véhicule relais", example = "Renault", nullable = true)
    private String marque;

    @Schema(description = "Modèle du véhicule relais", example = "Master", nullable = true)
    private String modele;

    @Schema(description = "Premier jour du relais", example = "2026-09-14")
    private LocalDate dateDebut;

    @Schema(description = "Dernier jour du relais (inclus), null tant que le véhicule n'est pas revenu", example = "2026-09-18", nullable = true)
    private LocalDate dateFin;

    @Schema(description = "Kilométrage du relais au départ", example = "45000", nullable = true)
    private Integer kmDebut;

    @Schema(description = "Kilométrage du relais au retour", example = "45850", nullable = true)
    private Integer kmFin;

    @Schema(description = "Motif du relais", example = "Garage", nullable = true)
    private String motif;

    @Schema(description = "Commentaire libre", nullable = true)
    private String commentaire;

    @Schema(description = "Statut à la date du jour : A_VENIR, EN_COURS ou TERMINE", example = "EN_COURS")
    private Statut statut;

    @Schema(description = "Dernier relevé kilométrique saisi pendant le relais", example = "45210", nullable = true)
    private Integer latestKm;

    @Schema(description = "Date du dernier relevé saisi pendant le relais", example = "2026-09-15T07:02:00+02:00", nullable = true)
    private ZonedDateTime latestKmDate;

    @Schema(description = "Nombre de relevés kilométriques saisis pendant le relais", example = "4")
    private Long nbReleves;

    @Schema(description = "Kilomètres parcourus : (km au retour, sinon dernier relevé) - km au départ ; null si inconnu", example = "850", nullable = true)
    private Integer kmParcourus;

    @Schema(description = "Date de création", example = "2026-09-14T08:30:00+02:00")
    private ZonedDateTime createdAt;

    @Schema(description = "Date de dernière modification", example = "2026-09-18T17:10:00+02:00", nullable = true)
    private ZonedDateTime updatedAt;
}
