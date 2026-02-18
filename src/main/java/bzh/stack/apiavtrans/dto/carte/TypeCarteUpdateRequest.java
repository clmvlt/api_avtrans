package bzh.stack.apiavtrans.dto.carte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to update a card type")
public class TypeCarteUpdateRequest {

    @Schema(description = "Card type name", example = "Bancaire")
    private String nom;

    @Schema(description = "Card type description", example = "Carte bancaire")
    private String description;
}
