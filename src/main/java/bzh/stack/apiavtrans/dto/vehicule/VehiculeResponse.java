package bzh.stack.apiavtrans.dto.vehicule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeResponse {
    private Boolean success;
    private String message;
    private VehiculeDTO vehicule;
}
