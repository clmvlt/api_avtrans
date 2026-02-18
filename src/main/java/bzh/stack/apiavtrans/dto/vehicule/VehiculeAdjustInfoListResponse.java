package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response for vehicle adjustment information")
public class VehiculeAdjustInfoListResponse {
    @Schema(description = "Request success status")
    private Boolean success;

    @Schema(description = "List of adjustment information")
    private List<VehiculeAdjustInfoDTO> adjustInfos;

    @Schema(description = "Current page number (0-indexed)")
    private Integer page;

    @Schema(description = "Page size")
    private Integer size;

    @Schema(description = "Total number of elements")
    private Long totalElements;

    @Schema(description = "Total number of pages")
    private Integer totalPages;

    public VehiculeAdjustInfoListResponse(Boolean success, List<VehiculeAdjustInfoDTO> adjustInfos) {
        this.success = success;
        this.adjustInfos = adjustInfos;
    }
}
