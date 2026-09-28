package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant un véhicule relais")
public class VehiculeRelaiResponse {

    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat")
    private String message;

    @Schema(description = "Véhicule relais")
    private VehiculeRelaiDTO relai;
}
