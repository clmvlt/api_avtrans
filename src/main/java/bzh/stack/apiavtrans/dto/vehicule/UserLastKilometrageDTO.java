package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's last kilometrage entry with today status")
public class UserLastKilometrageDTO {

    @Schema(description = "Last kilometrage entry by the user (null if none)")
    private VehiculeKilometrageDTO lastKilometrage;

    @Schema(description = "True if user has already entered a kilometrage today", example = "true")
    private Boolean hasEnteredToday;
}
