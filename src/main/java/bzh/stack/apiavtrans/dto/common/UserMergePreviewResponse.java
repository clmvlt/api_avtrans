package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant l'aperçu d'une fusion de profils")
public class UserMergePreviewResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat", example = "Aperçu de la fusion")
    private String message;

    @Schema(description = "Aperçu de la fusion")
    private UserMergePreviewDTO data;
}
