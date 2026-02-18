package bzh.stack.apiavtrans.dto.carte;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Schema(description = "Card representation")
public class CarteDTO {

    @Schema(description = "Card UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Schema(description = "Card name", example = "Carte entreprise")
    private String nom;

    @Schema(description = "Card description", example = "Carte principale pour les depenses")
    private String description;

    @Schema(description = "Card code/PIN", example = "1234")
    private String code;

    @Schema(description = "Card number", example = "4111111111111111")
    private String numero;

    @Schema(description = "User UUID who owns the card")
    private UUID userUuid;

    @Schema(description = "User who owns the card")
    private UserDTO user;

    @Schema(description = "Card type UUID")
    private UUID typeCarteUuid;

    @Schema(description = "Card type")
    private TypeCarteDTO typeCarte;

    @Schema(description = "Creation date")
    private ZonedDateTime createdAt;

    @Schema(description = "Last update date")
    private ZonedDateTime updatedAt;
}
