package bzh.stack.apiavtrans.dto.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Schema(description = "App version information")
public class AppVersionDTO {

    @Schema(description = "Unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Version code (numeric)", example = "10")
    private Integer versionCode;

    @Schema(description = "Version name", example = "1.2.3")
    private String versionName;

    @Schema(description = "Original file name", example = "avtrans-v1.2.3.apk")
    private String originalFileName;

    @Schema(description = "File size in bytes", example = "52428800")
    private Long fileSize;

    @Schema(description = "Changelog / release notes")
    private String changelog;

    @Schema(description = "Whether this version is active and available for download", example = "true")
    private Boolean isActive;

    @Schema(description = "Number of downloads", example = "150")
    private Long downloadCount;

    @Schema(description = "Download URL for this version")
    private String downloadUrl;

    @Schema(description = "Upload date", example = "2025-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;

    @Schema(description = "UUID of admin who uploaded this version")
    private UUID createdByUuid;

    @Schema(description = "Name of admin who uploaded this version")
    private String createdByName;
}
