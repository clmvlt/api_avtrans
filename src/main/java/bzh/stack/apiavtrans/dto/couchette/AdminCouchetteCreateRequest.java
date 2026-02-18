package bzh.stack.apiavtrans.dto.couchette;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin request to create a couchette for a user")
public class AdminCouchetteCreateRequest {
    @Schema(description = "User UUID")
    private UUID userUuid;

    @Schema(description = "Couchette date", example = "2025-01-15")
    private LocalDate date;
}
