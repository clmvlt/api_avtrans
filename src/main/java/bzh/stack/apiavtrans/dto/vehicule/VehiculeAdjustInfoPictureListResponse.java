package bzh.stack.apiavtrans.dto.vehicule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeAdjustInfoPictureListResponse {
    private Boolean success;
    private List<VehiculeAdjustInfoPictureDTO> pictures;
}
