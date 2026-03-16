package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant un article de stock")
public class StockItemDTO {

    @Schema(description = "Identifiant unique de l'article", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Référence/SKU unique de l'article", example = "FRN-PAD-001")
    private String reference;

    @Schema(description = "Nom de l'article", example = "Plaquettes de frein avant")
    private String nom;

    @Schema(description = "Description détaillée de l'article", example = "Plaquettes de frein avant pour véhicules utilitaires")
    private String description;

    @Schema(description = "Quantité en stock", example = "25")
    private Integer quantite;

    @Schema(description = "Catégorie de l'article")
    private StockCategoryDTO category;

    @Schema(description = "Unité de mesure", example = "pièce")
    private String unite;

    @Schema(description = "Prix unitaire HT", example = "12.50")
    private BigDecimal prixUnitaire;

    @Schema(description = "Date de création")
    private ZonedDateTime createdAt;

    @Schema(description = "Date de dernière modification")
    private ZonedDateTime updatedAt;
}
