package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour créer un nouveau véhicule")
public class VehiculeCreateRequest {

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String immat;

    @Schema(description = "Modèle du véhicule", example = "Transit")
    private String model;

    @Schema(description = "Marque du véhicule", example = "Ford")
    private String brand;

    @Schema(description = "Commentaire optionnel", example = "Véhicule de livraison principal")
    private String comment;

    @Schema(description = "Photo de profil du véhicule en base64", example = "data:image/png;base64,iVBORw0KGgo...", nullable = true)
    private String pictureBase64;
}
