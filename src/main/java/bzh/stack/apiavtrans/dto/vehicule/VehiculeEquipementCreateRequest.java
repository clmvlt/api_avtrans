package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création d'un équipement véhicule")
public class VehiculeEquipementCreateRequest {

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID vehiculeId;

    @Schema(description = "Nom de l'équipement", example = "Gilet jaune", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Quantité (par défaut 1)", example = "2")
    private Integer quantite;

    @Schema(description = "Commentaire", example = "Obligatoire dans le véhicule")
    private String commentaire;
}
