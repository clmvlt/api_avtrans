package bzh.stack.apiavtrans.dto.common;

import bzh.stack.apiavtrans.dto.service.HeuresPeriodeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Heures travaillées nettes d'un utilisateur sur les périodes en cours")
public class UserHoursDTO {
    private HeuresPeriodeDTO hoursDay;
    private HeuresPeriodeDTO hoursWeek;
    private HeuresPeriodeDTO hoursMonth;
    private HeuresPeriodeDTO hoursYear;
    private HeuresPeriodeDTO hoursLastMonth;

    @Schema(description = "Instant de calcul (heure serveur)")
    private ZonedDateTime calculeLe;
}
