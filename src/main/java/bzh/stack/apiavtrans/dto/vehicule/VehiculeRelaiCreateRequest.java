package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de déclaration d'un véhicule relais")
public class VehiculeRelaiCreateRequest {

    @Schema(description = "Identifiant du véhicule remplacé", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID vehiculeId;

    @Schema(description = "Immatriculation du véhicule relais", example = "EF-456-GH", requiredMode = Schema.RequiredMode.REQUIRED)
    private String immat;

    @Schema(description = "Marque du véhicule relais", example = "Renault")
    private String marque;

    @Schema(description = "Modèle du véhicule relais", example = "Master")
    private String modele;

    @Schema(description = "Premier jour du relais", example = "2026-09-14", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dateDebut;

    @Schema(description = "Dernier jour du relais (inclus), vide tant que le véhicule n'est pas revenu", example = "2026-09-18")
    private LocalDate dateFin;

    @Schema(description = "Kilométrage du relais au départ", example = "45000")
    private Integer kmDebut;

    @Schema(description = "Kilométrage du relais au retour", example = "45850")
    private Integer kmFin;

    @Schema(description = "Motif du relais", example = "Garage")
    private String motif;

    @Schema(description = "Commentaire libre")
    private String commentaire;
}
