package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un utilisateur")
public class UpdateUserRequest {
    @Schema(description = "Prénom de l'utilisateur", example = "John")
    private String firstName;

    @Schema(description = "Nom de l'utilisateur", example = "Doe")
    private String lastName;

    @Schema(description = "Indique si l'utilisateur est actif", example = "true")
    private Boolean isActive;

    @Schema(description = "UUID du rôle de l'utilisateur")
    private UUID roleUuid;

    @Schema(description = "Indique si l'utilisateur a une couchette", example = "false")
    private Boolean isCouchette;

    @Schema(description = "Adresse de l'utilisateur")
    private AddressDTO address;

    @Schema(description = "Numéro de permis de conduire", example = "12AB34567")
    private String driverLicenseNumber;

    @Schema(description = "Heures mensuelles du contrat de l'utilisateur", example = "151.67")
    private Double heureContrat;
}
