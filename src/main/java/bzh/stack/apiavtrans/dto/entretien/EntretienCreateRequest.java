package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a maintenance record")
public class EntretienCreateRequest {

    @Schema(description = "Vehicle identifier", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID vehiculeId;

    @Schema(description = "Maintenance type identifier", example = "123e4567-e89b-12d3-a456-426614174002", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID typeEntretienId;

    @Schema(description = "Maintenance date", example = "2024-01-15T10:30:00+01:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private ZonedDateTime dateEntretien;

    @Schema(description = "Mileage at the time of maintenance", example = "77000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer kilometrage;

    @Schema(description = "Maintenance comment", example = "Replacement of worn brake pads")
    private String commentaire;

    @Schema(description = "Maintenance cost in euros", example = "250.50")
    private Double cout;

    @Schema(description = "Files to attach to the maintenance record")
    private List<EntretienFileUploadRequest> files;
}
