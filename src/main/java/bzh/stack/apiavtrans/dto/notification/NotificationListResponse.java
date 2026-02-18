package bzh.stack.apiavtrans.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing list of notifications")
public class NotificationListResponse {
    @Schema(description = "Success status", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Notifications retrieved successfully")
    private String message;

    @Schema(description = "List of notifications")
    private List<NotificationDTO> notifications;
}
