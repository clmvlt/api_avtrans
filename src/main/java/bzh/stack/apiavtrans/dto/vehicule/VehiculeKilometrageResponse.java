package bzh.stack.apiavtrans.dto.vehicule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeKilometrageResponse {
    private Boolean success;
    private String message;
    private VehiculeKilometrageDTO kilometrage;
}
