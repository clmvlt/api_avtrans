package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing active service")
public class ActiveServiceResponse {
    @Schema(description = "Operation success status", example = "true")
    private Boolean success;

    @Schema(description = "Active service (null if no active service)")
    private ServiceDTO service;
}
