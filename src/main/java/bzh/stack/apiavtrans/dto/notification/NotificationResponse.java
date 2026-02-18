package bzh.stack.apiavtrans.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing notification data")
public class NotificationResponse {
    @Schema(description = "Success status", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Notification retrieved successfully")
    private String message;

    @Schema(description = "Notification data")
    private NotificationDTO notification;
}
