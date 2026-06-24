package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'authentification Google")
public class GoogleAuthResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(
        description = "Statut de l'authentification Google : " +
                "AUTHENTICATED (connecté, voir 'user'), " +
                "NEEDS_REGISTRATION (aucun compte, voir 'googleProfile' pour pré-remplir la création), " +
                "PENDING_ACTIVATION (compte créé, en attente d'activation par un administrateur)",
        example = "AUTHENTICATED"
    )
    private String status;

    @Schema(description = "Message décrivant le résultat", example = "Connexion réussie")
    private String message;

    @Schema(description = "Utilisateur connecté avec token (présent uniquement si status = AUTHENTICATED)")
    private AuthUserDTO user;

    @Schema(description = "Profil Google pour pré-remplir l'inscription (présent uniquement si status = NEEDS_REGISTRATION)")
    private GoogleProfileDTO googleProfile;
}
