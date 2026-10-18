package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCreateRequest {
    private UUID userUuid;
    private ZonedDateTime debut;
    private ZonedDateTime fin;
    private Double latitude;
    private Double longitude;
    private Double latitudeEnd;
    private Double longitudeEnd;
    private Boolean isBreak;

    @Schema(description = "Pause uniquement : UUID du service de travail auquel rattacher la pause. "
            + "Absent : la pause est rattachée au service du salarié en cours à son début.")
    private UUID serviceParentUuid;
}
