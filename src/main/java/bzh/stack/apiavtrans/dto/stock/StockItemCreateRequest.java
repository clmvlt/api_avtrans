package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour créer un article de stock")
public class StockItemCreateRequest {

    @NotBlank(message = "La référence est obligatoire")
    @Schema(description = "Référence/SKU unique de l'article", example = "FRN-PAD-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reference;

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom de l'article", example = "Plaquettes de frein avant", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Description détaillée de l'article", example = "Plaquettes de frein avant pour véhicules utilitaires")
    private String description;

    @NotNull(message = "La quantité est obligatoire")
    @PositiveOrZero(message = "La quantité doit être positive ou nulle")
    @Schema(description = "Quantité initiale en stock", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantite;

    @Schema(description = "UUID de la catégorie", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID categoryId;

    @Schema(description = "Unité de mesure", example = "pièce")
    private String unite;

    @PositiveOrZero(message = "Le prix unitaire doit être positif ou nul")
    @Schema(description = "Prix unitaire HT", example = "12.50")
    private BigDecimal prixUnitaire;
}
