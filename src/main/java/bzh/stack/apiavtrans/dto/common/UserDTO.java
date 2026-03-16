package bzh.stack.apiavtrans.dto.common;

import bzh.stack.apiavtrans.dto.notification.NotificationPreferencesDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data transfer object")
public class UserDTO {
    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Schema(description = "User email address", example = "jean.dupont@example.com")
    private String email;

    @Schema(description = "User first name", example = "Jean")
    private String firstName;

    @Schema(description = "User last name", example = "Dupont")
    private String lastName;

    @Schema(description = "Whether user email is verified", example = "true")
    private Boolean isMailVerified;

    @Schema(description = "Whether user account is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Account creation date")
    private ZonedDateTime createdAt;

    @Schema(description = "Account last update date")
    private ZonedDateTime updatedAt;

    @Schema(description = "User role")
    private RoleDTO role;

    @Schema(description = "Profile picture URL", example = "http://192.168.1.120:8081/uploads/pictures/user.jpg")
    private String pictureUrl;

    @Schema(description = "Whether user has a bunk bed", example = "false")
    private Boolean isCouchette;

    @Schema(description = "User address")
    private AddressDTO address;

    @Schema(description = "Driver license number", example = "12AB34567")
    private String driverLicenseNumber;

    @Schema(description = "Notification preferences")
    private NotificationPreferencesDTO notificationPreferences;

    @Schema(description = "Heures mensuelles du contrat", example = "151.67")
    private Double heureContrat;

    @Schema(description = "Presence status: PRESENT (active service), ON_BREAK (on break), ABSENT (no active service)",
            example = "PRESENT",
            allowableValues = {"PRESENT", "ON_BREAK", "ABSENT"})
    private String status;
}
