package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse de connexion d'un utilisateur")
public class LoginResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat", example = "Login successful")
    private String message;

    @Schema(description = "Données de l'utilisateur connecté avec token d'authentification")
    private AuthUserDTO user;
}
