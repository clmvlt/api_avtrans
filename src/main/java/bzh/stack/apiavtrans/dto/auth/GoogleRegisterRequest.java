package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création de compte via Google (étape de confirmation)")
public class GoogleRegisterRequest {
    @Schema(
        description = "Le même ID token Google que celui utilisé pour la connexion (toujours valide, < 1h)",
        example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String idToken;

    @Schema(description = "Prénom (pré-rempli depuis Google, modifiable par l'utilisateur)", example = "John")
    private String firstName;

    @Schema(description = "Nom (pré-rempli depuis Google, modifiable par l'utilisateur)", example = "Doe")
    private String lastName;
}
