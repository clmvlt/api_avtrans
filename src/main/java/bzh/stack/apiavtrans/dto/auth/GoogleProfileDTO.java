package bzh.stack.apiavtrans.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informations du profil Google utilisées pour pré-remplir la création de compte")
public class GoogleProfileDTO {
    @Schema(description = "Email Google (non modifiable, vérifié par Google)", example = "john.doe@gmail.com")
    private String email;

    @Schema(description = "Prénom proposé par Google", example = "John")
    private String firstName;

    @Schema(description = "Nom proposé par Google", example = "Doe")
    private String lastName;

    @Schema(description = "URL de la photo de profil Google", example = "https://lh3.googleusercontent.com/a/...")
    private String pictureUrl;
}
