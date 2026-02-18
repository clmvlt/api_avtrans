package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour créer un nouveau rapport de véhicule")
public class RapportVehiculeCreateRequest {

    @Schema(description = "UUID du véhicule concerné", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID vehiculeId;

    @Schema(description = "Commentaire du rapport", example = "Véhicule en bon état général, petite rayure côté conducteur")
    private String commentaire;

    @Schema(description = "Liste des images encodées en base64", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> picturesB64;
}
