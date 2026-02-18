package bzh.stack.apiavtrans.dto.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to update app version metadata")
public class AppVersionUpdateRequest {

    @Schema(description = "Changelog / release notes")
    private String changelog;

    @Schema(description = "Whether this version is active and available for download", example = "true")
    private Boolean isActive;
}
