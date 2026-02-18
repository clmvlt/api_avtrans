package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant un dossier de types d'entretien")
public class DossierTypeEntretienResponse {

    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat")
    private String message;

    @Schema(description = "Dossier de types d'entretien")
    private DossierTypeEntretienDTO dossier;
}
