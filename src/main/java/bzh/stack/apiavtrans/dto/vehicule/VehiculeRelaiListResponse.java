package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant l'historique des relais d'un véhicule")
public class VehiculeRelaiListResponse {

    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Relais du véhicule, du plus récent au plus ancien")
    private List<VehiculeRelaiDTO> relais;
}
