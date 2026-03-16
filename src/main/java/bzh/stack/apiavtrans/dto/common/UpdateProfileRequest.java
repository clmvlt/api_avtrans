package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour du profil utilisateur")
public class UpdateProfileRequest {
    @Schema(description = "Prénom", example = "Jean")
    private String firstName;

    @Schema(description = "Nom", example = "Dupont")
    private String lastName;

    @Schema(description = "Email", example = "jean.dupont@example.com")
    private String email;

    @Schema(description = "Photo de profil en base64")
    private String picture;

    @Schema(description = "Adresse de l'utilisateur")
    private AddressDTO address;

    @Schema(description = "Numéro de permis de conduire", example = "12AB34567")
    private String driverLicenseNumber;
}
