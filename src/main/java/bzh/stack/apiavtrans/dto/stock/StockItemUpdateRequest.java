package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour mettre à jour un article de stock")
public class StockItemUpdateRequest {

    @Schema(description = "Référence/SKU unique de l'article", example = "FRN-PAD-001")
    private String reference;

    @Schema(description = "Nom de l'article", example = "Plaquettes de frein avant")
    private String nom;

    @Schema(description = "Description détaillée de l'article", example = "Plaquettes de frein avant pour véhicules utilitaires")
    private String description;

    @PositiveOrZero(message = "La quantité doit être positive ou nulle")
    @Schema(description = "Quantité en stock", example = "30")
    private Integer quantite;

    @Schema(description = "UUID de la catégorie", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID categoryId;

    @Schema(description = "Unité de mesure", example = "pièce")
    private String unite;
}
