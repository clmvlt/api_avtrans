package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Maintenance file")
public class EntretienFileDTO {

    @Schema(description = "Unique file identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Maintenance record identifier", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID entretienId;

    @Schema(description = "Base64 encoded file content")
    private String fileB64;

    @Schema(description = "Original file name", example = "facture_entretien.pdf")
    private String originalName;

    @Schema(description = "File MIME type", example = "application/pdf")
    private String mimeType;

    @Schema(description = "File size in bytes", example = "102400")
    private Long fileSize;

    @Schema(description = "File upload date", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;

    @Schema(description = "File download URL", example = "http://192.168.1.120:8081/uploads/entretiens/file.pdf")
    private String fileUrl;
}
