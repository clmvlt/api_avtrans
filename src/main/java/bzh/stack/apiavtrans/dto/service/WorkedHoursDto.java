package bzh.stack.apiavtrans.dto.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Heures travaillées nettes par période. Un service compte en entier (pauses déduites) "
        + "sur la période de son jour de début, même s'il se termine le lendemain.")
public class WorkedHoursDto {
    @Schema(description = "Instant de calcul (heure serveur)")
    private ZonedDateTime calculeLe;

    @Schema(description = "Heures travaillées sur l'année")
    private HeuresPeriodeDTO year;

    @Schema(description = "Heures travaillées sur le mois")
    private HeuresPeriodeDTO month;

    @Schema(description = "Heures travaillées sur la semaine ISO")
    private HeuresPeriodeDTO week;

    @Schema(description = "Heures travaillées sur la journée (services débutés ce jour-là). Sans paramètre de date : "
            + "journée de travail en cours, celle de la veille pour un service de nuit en cours ou terminé après minuit")
    private HeuresPeriodeDTO day;

    @Schema(description = "Jour de la période day", example = "2026-10-12")
    private LocalDate jour;

    @Schema(description = "Heures travaillées sur le mois dernier")
    private HeuresPeriodeDTO lastMonth;
}
