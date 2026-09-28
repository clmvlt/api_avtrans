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

    @Schema(description = "Heures créditées par les absences approuvées du mois (0 sans contrat)", example = "35.0")
    private Double heuresAbsences;

    @Schema(description = "Heures créditées par les jours fériés chômés du mois (lun.-sam., non pointés, hors sans solde)", example = "5.83")
    private Double heuresFeries;

    @Schema(description = "Nombre de jours fériés chômés crédités dans le mois", example = "1")
    private Integer joursFeries;

    @Schema(description = "Total : heures effectuées + absences + jours fériés", example = "150.5")
    private Double heuresTotal;

    @Schema(description = "Différence (total - contrat), null sans contrat", example = "-1.17")
    private Double differenceTotal;

    @Schema(description = "Pourcentage de réalisation du contrat avec les heures créditées, null sans contrat", example = "99.23")
    private Double pourcentageTotal;

    @Schema(description = "Jours ouvrés restants d'aujourd'hui inclus à la fin du mois (lun.-ven., fériés et absences approuvées déduits, demi-journée = 0,5) ; 0 pour un mois passé", example = "5.0")
    private Double joursOuvresRestants;

    @Schema(description = "Heures d'un jour ouvré selon le contrat (heures hebdomadaires / 5), null sans contrat", example = "7.0")
    private Double heuresParJourContrat;

    @Schema(description = "Heures encore attendues d'ici la fin du mois au rythme du contrat (heures déjà pointées aujourd'hui déduites), null sans contrat", example = "35.0")
    private Double heuresRestantesPrevues;

    @Schema(description = "Prévision du total à la fin du mois : total actuel + heures restantes prévues, null sans contrat", example = "152.5")
    private Double heuresPrevisionnelles;

    @Schema(description = "Différence (prévision - contrat), null sans contrat", example = "0.83")
    private Double differencePrevisionnelle;
}
