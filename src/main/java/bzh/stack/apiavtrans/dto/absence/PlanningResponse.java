package bzh.stack.apiavtrans.dto.absence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningResponse {
    private boolean success;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType;
    private List<PlanningUserDTO> users;
}
