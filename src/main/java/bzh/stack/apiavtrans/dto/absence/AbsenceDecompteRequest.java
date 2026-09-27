package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aperçu du décompte d'une absence (jours et heures créditées) avant sa création ou sa modification")
public class AbsenceDecompteRequest {

    @NotNull(message = "La date de début est obligatoire")
    @Schema(description = "Date de début (incluse)", example = "2026-10-05", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Schema(description = "Date de fin (incluse)", example = "2026-10-09", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Schema(description = "Période : FULL_DAY, MORNING ou AFTERNOON", example = "FULL_DAY")
    private String period;

    @Schema(description = "UUID du type d'absence (absent = type personnalisé : jours ouvrables, crédite des heures)")
    private UUID absenceTypeUuid;

    @Schema(description = "UUID de l'employé (route administrateur uniquement ; ignoré sinon)")
    private UUID userUuid;
}
