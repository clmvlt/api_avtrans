package bzh.stack.apiavtrans.dto.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to upload a new app version")
public class AppVersionUploadRequest {

    @Schema(description = "Base64 encoded APK file content", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apkB64;

    @Schema(description = "Version code (numeric, must be unique and higher than previous)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer versionCode;

    @Schema(description = "Version name", example = "1.2.3", requiredMode = Schema.RequiredMode.REQUIRED)
    private String versionName;

    @Schema(description = "Original file name", example = "avtrans-v1.2.3.apk")
    private String originalFileName;

    @Schema(description = "Changelog / release notes")
    private String changelog;
}
