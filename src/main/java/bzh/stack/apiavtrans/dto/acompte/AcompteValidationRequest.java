package bzh.stack.apiavtrans.dto.acompte;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteValidationRequest {
    @NotNull(message = "La décision est obligatoire (true pour approuver, false pour refuser)")
    private Boolean approved;

    private String rejectionReason;
}
