package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour créer une catégorie de stock")
public class StockCategoryCreateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom de la catégorie", example = "Freins", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Description de la catégorie", example = "Pièces liées au système de freinage")
    private String description;
}
