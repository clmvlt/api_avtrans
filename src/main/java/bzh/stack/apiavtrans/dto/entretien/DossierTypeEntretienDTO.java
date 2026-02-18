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
@Schema(description = "Dossier de types d'entretien (catégorie)")
public class DossierTypeEntretienDTO {

    @Schema(description = "Identifiant unique du dossier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nom du dossier", example = "Freinage")
    private String nom;

    @Schema(description = "Description du dossier", example = "Tous les entretiens liés au système de freinage")
    private String description;

    @Schema(description = "Date de création", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;
}
