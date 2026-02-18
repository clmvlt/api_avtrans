package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response for vehicle reports")
public class RapportVehiculeListResponse {

    @Schema(description = "Request success status", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Rapports récupérés avec succès")
    private String message;

    @Schema(description = "List of reports")
    private List<RapportVehiculeDTO> data;

    @Schema(description = "Current page number (0-indexed), null if all results returned")
    private Integer page;

    @Schema(description = "Page size, null if all results returned")
    private Integer size;

    @Schema(description = "Total number of elements")
    private Long totalElements;

    @Schema(description = "Total number of pages, null if all results returned")
    private Integer totalPages;

    public RapportVehiculeListResponse(Boolean success, String message, List<RapportVehiculeDTO> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
