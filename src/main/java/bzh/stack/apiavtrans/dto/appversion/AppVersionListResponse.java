package bzh.stack.apiavtrans.dto.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "List of app versions response")
public class AppVersionListResponse {

    @Schema(description = "Operation success status", example = "true")
    private Boolean success;

    @Schema(description = "List of app versions")
    private List<AppVersionDTO> versions;
}
