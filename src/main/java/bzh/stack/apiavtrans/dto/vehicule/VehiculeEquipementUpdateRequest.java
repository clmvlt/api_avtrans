package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un équipement véhicule")
public class VehiculeEquipementUpdateRequest {

    @Schema(description = "Nom de l'équipement", example = "Gilet jaune haute visibilité")
    private String nom;

    @Schema(description = "Quantité", example = "3")
    private Integer quantite;

    @Schema(description = "Commentaire", example = "Mis à jour le 15/03")
    private String commentaire;
}
