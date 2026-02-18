package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un entretien")
public class EntretienUpdateRequest {

    @Schema(description = "Identifiant du type d'entretien", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID typeEntretienId;

    @Schema(description = "Date de l'entretien", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime dateEntretien;

    @Schema(description = "Kilométrage au moment de l'entretien", example = "77000")
    private Integer kilometrage;

    @Schema(description = "Commentaire sur l'entretien", example = "Remplacement des plaquettes usées")
    private String commentaire;

    @Schema(description = "Coût de l'entretien en euros", example = "250.50")
    private Double cout;
}
