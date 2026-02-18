package bzh.stack.apiavtrans.dto.vehicule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeAdjustInfoResponse {
    private Boolean success;
    private String message;
    private VehiculeAdjustInfoDTO adjustInfo;
}
