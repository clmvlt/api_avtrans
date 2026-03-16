package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comparaison heures contrat vs heures effectuées pour un utilisateur")
public class UserContractComparisonDTO {

    @Schema(description = "Informations de l'utilisateur")
    private UserDTO user;

    @Schema(description = "Année de la période", example = "2026")
    private Integer year;

    @Schema(description = "Mois de la période (1-12)", example = "3")
    private Integer month;

    @Schema(description = "Heures mensuelles prévues au contrat", example = "151.67")
    private Double heureContrat;

    @Schema(description = "Heures effectivement travaillées sur la période", example = "142.5")
    private Double heuresEffectuees;

    @Schema(description = "Différence (effectuées - contrat). Positif = heures sup, Négatif = sous le contrat", example = "-9.17")
    private Double difference;

    @Schema(description = "Pourcentage de réalisation du contrat", example = "93.95")
    private Double pourcentageRealisation;

    @Schema(description = "Nombre de jours d'absence approuvés sur la période", example = "2.5")
    private Double joursAbsence;

    @Schema(description = "Nombre de jours ouvrés dans le mois", example = "22")
    private Integer joursOuvres;

    @Schema(description = "Nombre de jours effectivement travaillés (au moins un service)", example = "19")
    private Integer joursTravailles;

    @Schema(description = "Heures moyennes par jour travaillé", example = "7.5")
    private Double moyenneHeuresParJour;
}
