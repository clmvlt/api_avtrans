package bzh.stack.apiavtrans.dto.carte;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Request to create a new card")
public class CarteCreateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Card name", example = "Carte entreprise", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Card description", example = "Carte principale pour les depenses")
    private String description;

    @Schema(description = "Card code/PIN", example = "1234")
    private String code;

    @Schema(description = "Card number", example = "4111111111111111")
    private String numero;

    @Schema(description = "Card expiration date", example = "2026-12-31")
    private LocalDate dateExpiration;

    @Schema(description = "User UUID who will own the card (optional)")
    private UUID userUuid;

    @NotNull(message = "Le type de carte est obligatoire")
    @Schema(description = "Card type UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID typeCarteUuid;
}
