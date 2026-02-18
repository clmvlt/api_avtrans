package bzh.stack.apiavtrans.dto.absence;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceValidationRequest {

    @NotNull(message = "La décision est obligatoire")
    private Boolean approved;

    private String rejectionReason;
}
