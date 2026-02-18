package bzh.stack.apiavtrans.dto.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response for update check")
public class AppVersionCheckResponse {

    @Schema(description = "Operation success status", example = "true")
    private Boolean success;

    @Schema(description = "Whether an update is available", example = "true")
    private Boolean updateAvailable;

    @Schema(description = "Current version code provided in request", example = "5")
    private Integer currentVersionCode;

    @Schema(description = "Latest available version code", example = "10")
    private Integer latestVersionCode;

    @Schema(description = "Latest version details (null if no update available)")
    private AppVersionDTO latestVersion;
}
