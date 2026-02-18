package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant une liste d'articles de stock")
public class StockItemListResponse {

    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Liste des articles de stock")
    private List<StockItemDTO> stockItems;
}
