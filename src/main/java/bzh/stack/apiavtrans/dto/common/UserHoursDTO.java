package bzh.stack.apiavtrans.dto.common;

import bzh.stack.apiavtrans.dto.service.HeuresPeriodeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Heures travaillées nettes d'un utilisateur sur les périodes en cours")
public class UserHoursDTO {
    @Schema(description = "Journée de travail en cours (celle de la veille pour un service de nuit en cours ou terminé après minuit)")
    private HeuresPeriodeDTO hoursDay;
    private HeuresPeriodeDTO hoursWeek;
    private HeuresPeriodeDTO hoursMonth;
    private HeuresPeriodeDTO hoursYear;
    private HeuresPeriodeDTO hoursLastMonth;

    @Schema(description = "Instant de calcul (heure serveur)")
    private ZonedDateTime calculeLe;

    @Schema(description = "Jour de rattachement de hoursDay", example = "2026-10-12")
    private LocalDate jour;
}
