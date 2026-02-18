package bzh.stack.apiavtrans.dto.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Heures travaillées par période")
public class WorkedHoursDto {
    @Schema(description = "Heures travaillées sur l'année en cours", example = "1840.50")
    private Double year;

    @Schema(description = "Heures travaillées sur le mois en cours", example = "160.25")
    private Double month;

    @Schema(description = "Heures travaillées sur la semaine en cours", example = "40.75")
    private Double week;

    @Schema(description = "Heures travaillées aujourd'hui", example = "8.50")
    private Double day;

    @Schema(description = "Heures travaillées sur le mois dernier", example = "152.75")
    private Double lastMonth;
}
