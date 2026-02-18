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
@Schema(description = "Request to search couchettes with filters and pagination")
public class CouchetteSearchRequest {
    @Schema(description = "Filter by user UUID")
    private UUID userUuid;

    @Schema(description = "Filter by start date (inclusive)")
    private LocalDate startDate;

    @Schema(description = "Filter by end date (inclusive)")
    private LocalDate endDate;

    @Schema(description = "Page number (0-indexed)", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "20")
    private Integer size;

    @Schema(description = "Sort by field", example = "date")
    private String sortBy;

    @Schema(description = "Sort direction (asc/desc)", example = "desc")
    private String sortDirection;
}
