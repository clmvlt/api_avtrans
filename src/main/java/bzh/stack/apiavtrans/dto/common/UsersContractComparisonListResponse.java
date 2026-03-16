package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant la comparaison contrat de tous les utilisateurs")
public class UsersContractComparisonListResponse {

    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat")
    private String message;

    @Schema(description = "Année de la période", example = "2026")
    private Integer year;

    @Schema(description = "Mois de la période (1-12)", example = "3")
    private Integer month;

    @Schema(description = "Liste des comparaisons par utilisateur")
    private List<UserContractComparisonDTO> users;
}
