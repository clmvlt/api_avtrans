package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to upload a file to a vehicle")
public class VehiculeFileUploadRequest {

    @Schema(description = "Base64 encoded file content", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileB64;

    @Schema(description = "Original file name", example = "carte_grise.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    @Schema(description = "File MIME type", example = "application/pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mimeType;
}
