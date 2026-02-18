package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un type d'entretien générique")
public class TypeEntretienUpdateRequest {

    @Schema(description = "Nom du type d'entretien", example = "Changement freins avant")
    private String nom;

    @Schema(description = "Description détaillée", example = "Remplacement des plaquettes et disques de frein avant")
    private String description;

    @Schema(description = "Identifiant du dossier parent (null pour retirer du dossier)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID dossierId;
}
