package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Décompte d'une absence : jours décomptés et heures créditées, jour par jour")
public class AbsenceDecompteDTO {

    @Schema(description = "Mode de décompte : JOURS_OUVRABLES (lun.-sam.), JOURS_OUVRES (lun.-ven.) ou JOURS_CALENDAIRES",
            example = "JOURS_OUVRABLES")
    private String modeDecompte;

    @Schema(description = "Le type d'absence crédite-t-il des heures ?", example = "true")
    private Boolean compteHeures;

    @Schema(description = "L'employé a-t-il des heures de contrat renseignées ? (sinon 0 h)", example = "true")
    private Boolean contratRenseigne;

    @Schema(description = "Heures mensuelles du contrat", example = "151.67")
    private Double heureContratMensuel;

    @Schema(description = "Heures hebdomadaires équivalentes (mensuel × 12 / 52)", example = "35.0")
    private Double heuresHebdo;

    @Schema(description = "Valeur d'un jour décompté (hebdomadaire / jours par semaine du mode)", example = "5.83")
    private Double heuresParJour;

    @Schema(description = "Nombre de jours décomptés (0.5 par demi-journée)", example = "6")
    private Double joursDecomptes;

    @Schema(description = "Heures calculées automatiquement", example = "35.0")
    private Double heuresCalculees;

    @Schema(description = "Heures fixées à la main par un administrateur (null = calcul automatique)", example = "null")
    private Double heuresForcees;

    @Schema(description = "Heures retenues (forcées sinon calculées)", example = "35.0")
    private Double heures;

    @Schema(description = "Détail jour par jour, y compris les jours non décomptés et le samedi ajouté")
    private List<JourDecompteDTO> jours;
}
