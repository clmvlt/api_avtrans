package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Journée de travail en cours : services de travail rattachés à un jour (jour de début), avec leurs pauses. "
        + "Après minuit, un salarié de nuit retrouve la journée de son service de la veille tant qu'il n'a pas repris de service.")
public class JourneeTravailResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Jour de rattachement de la journée (jour de début de ses services)", example = "2026-10-12")
    private LocalDate date;

    @Schema(description = "true si la journée est celle du jour calendaire en cours, false pour la journée d'un service de nuit commencé la veille",
            example = "false")
    private Boolean aujourdhui;

    @Schema(description = "Temps de travail net de la journée (pauses déduites), à calculeLe")
    private HeuresPeriodeDTO heures;

    @Schema(description = "Services de travail de la journée par début croissant, chacun avec ses pauses")
    private List<ServiceDTO> services;

    @Schema(description = "Instant de calcul (heure serveur)")
    private ZonedDateTime calculeLe;
}
