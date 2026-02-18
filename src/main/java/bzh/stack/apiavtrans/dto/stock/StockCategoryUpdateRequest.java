package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour mettre à jour une catégorie de stock")
public class StockCategoryUpdateRequest {

    @Schema(description = "Nom de la catégorie", example = "Freins")
    private String nom;

    @Schema(description = "Description de la catégorie", example = "Pièces liées au système de freinage")
    private String description;
}
