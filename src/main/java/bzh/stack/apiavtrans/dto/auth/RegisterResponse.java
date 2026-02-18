package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'enregistrement d'un utilisateur")
public class RegisterResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat", example = "User registered successfully. Please check your email to verify your account.")
    private String message;

    @Schema(description = "UUID de l'utilisateur créé", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;
}
