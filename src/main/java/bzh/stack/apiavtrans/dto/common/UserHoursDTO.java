package bzh.stack.apiavtrans.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserHoursDTO {
    private Double hoursDay;
    private Double hoursWeek;
    private Double hoursMonth;
    private Double hoursYear;
    private Double hoursLastMonth;
}
