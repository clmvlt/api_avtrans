package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de modification d'une absence par un administrateur (uniquement si non approuvée)")
public class AdminAbsenceUpdateRequest {

    @Schema(description = "Date de début de l'absence", example = "2026-04-01")
    private LocalDate startDate;

    @Schema(description = "Date de fin de l'absence", example = "2026-04-05")
    private LocalDate endDate;

    @Schema(description = "Motif de l'absence", example = "Congés payés")
    private String reason;

    @Schema(description = "UUID du type d'absence")
    private UUID absenceTypeUuid;

    @Schema(description = "Type personnalisé", example = "Formation externe")
    private String customType;

    @Schema(description = "Période : FULL_DAY, MORNING ou AFTERNOON", example = "FULL_DAY")
    private String period;
}
