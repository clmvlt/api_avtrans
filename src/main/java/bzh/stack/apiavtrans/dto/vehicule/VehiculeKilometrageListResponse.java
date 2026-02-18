package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response for vehicle mileage history")
public class VehiculeKilometrageListResponse {
    @Schema(description = "Request success status")
    private Boolean success;

    @Schema(description = "List of mileage records")
    private List<VehiculeKilometrageDTO> kilometrages;

    @Schema(description = "Current page number (0-indexed), null if all results returned")
    private Integer page;

    @Schema(description = "Page size, null if all results returned")
    private Integer size;

    @Schema(description = "Total number of elements")
    private Long totalElements;

    @Schema(description = "Total number of pages, null if all results returned")
    private Integer totalPages;

    public VehiculeKilometrageListResponse(Boolean success, List<VehiculeKilometrageDTO> kilometrages) {
        this.success = success;
        this.kilometrages = kilometrages;
    }
}
