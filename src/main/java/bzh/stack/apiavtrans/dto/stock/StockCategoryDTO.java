package bzh.stack.apiavtrans.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant une catégorie de stock")
public class StockCategoryDTO {

    @Schema(description = "Identifiant unique de la catégorie", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nom de la catégorie", example = "Freins")
    private String nom;

    @Schema(description = "Description de la catégorie", example = "Pièces liées au système de freinage")
    private String description;

    @Schema(description = "Date de création")
    private ZonedDateTime createdAt;

    @Schema(description = "Date de dernière modification")
    private ZonedDateTime updatedAt;
}
