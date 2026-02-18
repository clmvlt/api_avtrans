package bzh.stack.apiavtrans.dto.vehicule;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin request to update an existing mileage record")
public class VehiculeKilometrageUpdateRequest {

    @Positive(message = "Mileage must be positive")
    @Schema(description = "Updated mileage value", example = "126000")
    private Integer km;

    @Schema(description = "User who recorded this mileage", example = "456e7890-e89b-12d3-a456-426614174000")
    private UUID userUuid;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", shape = JsonFormat.Shape.STRING)
    @Schema(description = "Updated date/time for the record", example = "2025-01-16T10:00:00+01:00")
    private ZonedDateTime createdAt;
}
