package bzh.stack.apiavtrans.dto.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single app version response")
public class AppVersionResponse {

    @Schema(description = "Operation success status", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Version uploaded successfully")
    private String message;

    @Schema(description = "App version details")
    private AppVersionDTO version;
}
