package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Temps de travail net sur une période, à l'instant calculeLe de la réponse. "
        + "Affichage en direct : si enCours, ajouter le temps écoulé depuis la réception de la réponse à secondes.")
public class HeuresPeriodeDTO {

    @Schema(description = "Temps de travail net en secondes (pauses déduites)", example = "27000")
    private Long secondes;

    @Schema(description = "Temps de travail net en heures décimales, arrondi au centième", example = "7.5")
    private Double heures;

    @Schema(description = "true si un service de la période est en cours hors pause : le compteur augmente d'une seconde par seconde",
            example = "true")
    private Boolean enCours;
}
