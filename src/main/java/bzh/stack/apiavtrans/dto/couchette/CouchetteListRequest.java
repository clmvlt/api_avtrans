package bzh.stack.apiavtrans.dto.couchette;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to list couchettes with pagination")
public class CouchetteListRequest {
    @Schema(description = "Page number (0-indexed)", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "10")
    private Integer size;

    @Schema(description = "Filter by user UUID (admin only)")
    private UUID userUuid;
}
