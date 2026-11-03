package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aperçu de la fusion de deux profils, avant confirmation")
public class UserMergePreviewDTO {
    @Schema(description = "Profil conservé : ses informations (e-mail, rôle, contrat, coordonnées…) restent inchangées")
    private UserDTO conserve;

    @Schema(description = "Profil supprimé après le transfert de ses données")
    private UserDTO supprime;

    @Schema(description = "Données du profil supprimé transférées au profil conservé (types sans donnée omis)")
    private List<UserMergeItemDTO> donnees;

    @Schema(description = "Points à vérifier, qui n'empêchent pas la fusion (chevauchements, doublons, rôle…)")
    private List<String> avertissements;

    @Schema(description = "Raisons qui empêchent la fusion (vide si elle est possible)")
    private List<String> blocages;

    @Schema(description = "true si aucune raison n'empêche la fusion", example = "true")
    private Boolean fusionPossible;
}
