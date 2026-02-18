package bzh.stack.apiavtrans.dto.notification;

import bzh.stack.apiavtrans.entity.NotificationPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user notification preferences")
public class UpdateNotificationPreferencesRequest {

    @Schema(description = "Preference for acompte (advance request) notifications", example = "SITE")
    private NotificationPreference acompte;

    @Schema(description = "Preference for absence notifications", example = "EMAIL")
    private NotificationPreference absence;

    @Schema(description = "Preference for new user created notifications", example = "SITE")
    private NotificationPreference userCreated;

    @Schema(description = "Preference for vehicle report notifications", example = "EMAIL")
    private NotificationPreference rapportVehicule;

    @Schema(description = "Preference for todo notifications", example = "NONE")
    private NotificationPreference todo;
}
