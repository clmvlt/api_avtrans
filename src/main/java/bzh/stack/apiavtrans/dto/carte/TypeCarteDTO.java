package bzh.stack.apiavtrans.dto.carte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Schema(description = "Card type representation")
public class TypeCarteDTO {

    @Schema(description = "Card type UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Schema(description = "Card type name", example = "Bancaire")
    private String nom;

    @Schema(description = "Card type description", example = "Carte bancaire")
    private String description;

    @Schema(description = "Creation date")
    private ZonedDateTime createdAt;

    @Schema(description = "Last update date")
    private ZonedDateTime updatedAt;
}
