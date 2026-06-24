package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'authentification via Google (Sign in with Google)")
public class GoogleAuthRequest {
    @Schema(
        description = "ID token (JWT credential) retourné par Google Identity Services côté frontend",
        example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String idToken;
}
