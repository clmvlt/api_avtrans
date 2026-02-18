package bzh.stack.apiavtrans.dto.absence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceTypeResponse {
    private boolean success;
    private String message;
    private AbsenceTypeDTO absenceType;
}
