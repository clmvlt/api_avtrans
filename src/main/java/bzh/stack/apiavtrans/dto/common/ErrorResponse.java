package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'erreur standard")
public class ErrorResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "false")
    private Boolean success;

    @Schema(description = "Message d'erreur", example = "Une erreur s'est produite")
    private String message;
}
