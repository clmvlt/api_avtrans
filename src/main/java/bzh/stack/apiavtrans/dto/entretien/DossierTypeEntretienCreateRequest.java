package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création d'un dossier de types d'entretien")
public class DossierTypeEntretienCreateRequest {

    @Schema(description = "Nom du dossier", example = "Freinage", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @Schema(description = "Description du dossier", example = "Tous les entretiens liés au système de freinage")
    private String description;
}
