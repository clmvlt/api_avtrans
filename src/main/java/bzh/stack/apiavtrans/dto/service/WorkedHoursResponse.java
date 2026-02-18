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
@Schema(description = "Réponse contenant les heures travaillées par période")
public class WorkedHoursResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message d'erreur si applicable")
    private String message;

    @Schema(description = "Heures travaillées sur l'année en cours", example = "1840")
    private Long year;

    @Schema(description = "Heures travaillées sur le mois en cours", example = "160")
    private Long month;

    @Schema(description = "Heures travaillées sur la semaine en cours", example = "40")
    private Long week;

    @Schema(description = "Heures travaillées aujourd'hui", example = "8")
    private Long day;
}
