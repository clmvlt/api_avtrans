package bzh.stack.apiavtrans.dto.couchette;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Couchette data")
public class CouchetteDTO {
    @Schema(description = "Couchette UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;

    @Schema(description = "Couchette date", example = "2025-01-15")
    private LocalDate date;

    @Schema(description = "User who has the couchette")
    private UserDTO user;

    @Schema(description = "Creation timestamp")
    private ZonedDateTime createdAt;
}
