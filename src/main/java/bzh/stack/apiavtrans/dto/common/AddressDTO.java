package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Adresse d'un utilisateur")
public class AddressDTO {
    @Schema(description = "Rue et numéro", example = "12 rue de la Paix")
    private String street;

    @Schema(description = "Ville", example = "Paris")
    private String city;

    @Schema(description = "Code postal", example = "75002")
    private String postalCode;

    @Schema(description = "Pays", example = "France")
    private String country;
}
