package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un dossier de types d'entretien")
public class DossierTypeEntretienUpdateRequest {

    @Schema(description = "Nom du dossier", example = "Freinage")
    private String nom;

    @Schema(description = "Description du dossier", example = "Tous les entretiens liés au système de freinage")
    private String description;
}
