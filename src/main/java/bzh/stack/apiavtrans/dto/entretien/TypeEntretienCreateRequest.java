package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création d'un type d'entretien générique")
public class TypeEntretienCreateRequest {

    @Schema(description = "Nom du type d'entretien", example = "Changement freins avant", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Description détaillée", example = "Remplacement des plaquettes et disques de frein avant")
    private String description;

    @Schema(description = "Identifiant du dossier parent", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID dossierId;
}
