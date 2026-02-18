package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant un rapport de véhicule")
public class RapportVehiculeResponse {

    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message de retour", example = "Rapport récupéré avec succès")
    private String message;

    @Schema(description = "Données du rapport")
    private RapportVehiculeDTO data;
}
