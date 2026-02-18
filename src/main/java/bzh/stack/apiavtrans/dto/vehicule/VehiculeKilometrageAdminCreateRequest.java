package bzh.stack.apiavtrans.dto.vehicule;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin request to create a mileage record with custom date/time")
public class VehiculeKilometrageAdminCreateRequest {

    @NotNull(message = "Vehicle ID is required")
    @Schema(description = "Vehicle identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID vehiculeId;

    @NotNull(message = "Mileage is required")
    @Positive(message = "Mileage must be positive")
    @Schema(description = "Mileage to record", example = "125000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer km;

    @Schema(description = "User who recorded this mileage (optional)", example = "456e7890-e89b-12d3-a456-426614174000")
    private UUID userUuid;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", shape = JsonFormat.Shape.STRING)
    @Schema(description = "Custom date/time for the record (optional, defaults to now)", example = "2025-01-15T14:20:00+01:00")
    private ZonedDateTime createdAt;
}
