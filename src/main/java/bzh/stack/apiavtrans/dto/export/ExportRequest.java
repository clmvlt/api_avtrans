package bzh.stack.apiavtrans.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'export des heures de travail")
public class ExportRequest {

    @Schema(description = "Liste des UUIDs des utilisateurs à exporter", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    private List<UUID> userUuids;

    @Schema(description = "Date de début de la période", example = "2025-11-01")
    private LocalDate startDate;

    @Schema(description = "Date de fin de la période", example = "2025-11-30")
    private LocalDate endDate;
}
