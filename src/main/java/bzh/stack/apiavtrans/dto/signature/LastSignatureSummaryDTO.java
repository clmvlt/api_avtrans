package bzh.stack.apiavtrans.dto.signature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastSignatureSummaryDTO {
    private ZonedDateTime date;
    private Double heuresSignees;
    private Boolean needsToSign;
    private Double heuresLastMonth;
}
