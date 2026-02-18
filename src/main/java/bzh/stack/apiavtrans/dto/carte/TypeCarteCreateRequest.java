package bzh.stack.apiavtrans.dto.carte;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to create a new card type")
public class TypeCarteCreateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Card type name", example = "Bancaire", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Card type description", example = "Carte bancaire")
    private String description;
}
