package bzh.stack.apiavtrans.dto.carte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Request to update a card")
public class CarteUpdateRequest {

    @Schema(description = "Card name", example = "Carte entreprise")
    private String nom;

    @Schema(description = "Card description", example = "Carte principale pour les depenses")
    private String description;

    @Schema(description = "Card code/PIN", example = "1234")
    private String code;

    @Schema(description = "Card number", example = "4111111111111111")
    private String numero;

    @Schema(description = "Card expiration date", example = "2026-12-31")
    private LocalDate dateExpiration;

    @Schema(description = "User UUID who will own the card (set to null to remove user)")
    private UUID userUuid;

    @Schema(description = "Set to true to explicitly remove the user from the card")
    private Boolean clearUser;

    @Schema(description = "Card type UUID")
    private UUID typeCarteUuid;
}
