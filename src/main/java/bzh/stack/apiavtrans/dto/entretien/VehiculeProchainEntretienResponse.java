package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant un véhicule avec ses prochains entretiens")
public class VehiculeProchainEntretienResponse {

    @Schema(description = "Indique si la requête a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Véhicule avec ses prochains entretiens")
    private VehiculeProchainEntretienDTO data;
}
