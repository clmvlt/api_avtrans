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
@Schema(description = "Type d'entretien générique")
public class TypeEntretienDTO {

    @Schema(description = "Identifiant unique du type d'entretien", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nom du type d'entretien", example = "Changement freins avant")
    private String nom;

    @Schema(description = "Description détaillée", example = "Remplacement des plaquettes et disques de frein avant")
    private String description;

    @Schema(description = "Dossier du type d'entretien")
    private DossierTypeEntretienDTO dossier;

    @Schema(description = "Date de création", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;
}
