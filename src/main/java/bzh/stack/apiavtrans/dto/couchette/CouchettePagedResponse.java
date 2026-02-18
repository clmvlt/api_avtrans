package bzh.stack.apiavtrans.dto.couchette;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response for couchettes")
public class CouchettePagedResponse {
    @Schema(description = "Request success status", example = "true")
    private boolean success;

    @Schema(description = "List of couchettes")
    private List<CouchetteDTO> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of elements", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Is this the first page", example = "true")
    private boolean first;

    @Schema(description = "Is this the last page", example = "false")
    private boolean last;
}
