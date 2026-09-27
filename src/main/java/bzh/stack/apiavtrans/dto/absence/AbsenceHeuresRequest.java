package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Heures d'une absence fixées à la main par un administrateur")
public class AbsenceHeuresRequest {

    @Schema(description = "Heures créditées par l'absence ; null pour revenir au calcul automatique", example = "35")
    private Double heures;
}
