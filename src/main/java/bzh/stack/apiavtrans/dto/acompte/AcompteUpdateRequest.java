package bzh.stack.apiavtrans.dto.acompte;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteUpdateRequest {
    @Positive(message = "Le montant doit être positif")
    private Double montant;

    private String raison;

    private Boolean isPaid;
}
