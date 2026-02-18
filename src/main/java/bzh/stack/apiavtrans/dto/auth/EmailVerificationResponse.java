package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse de vérification d'email")
public class EmailVerificationResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat", example = "Email verified successfully")
    private String message;

    @Schema(description = "Email vérifié", example = "user@example.com")
    private String email;
}
