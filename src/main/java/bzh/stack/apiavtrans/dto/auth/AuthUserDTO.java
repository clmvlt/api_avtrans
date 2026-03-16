package bzh.stack.apiavtrans.dto.auth;

import bzh.stack.apiavtrans.dto.common.AddressDTO;
import bzh.stack.apiavtrans.dto.common.RoleDTO;
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
@Schema(description = "User data with authentication token")
public class AuthUserDTO {
    @Schema(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;

    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User first name", example = "John")
    private String firstName;

    @Schema(description = "User last name", example = "Doe")
    private String lastName;

    @Schema(description = "Email verification status", example = "true")
    private Boolean isMailVerified;

    @Schema(description = "Account activation status", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp")
    private ZonedDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private ZonedDateTime updatedAt;

    @Schema(description = "User role")
    private RoleDTO role;

    @Schema(description = "Authentication token")
    private String token;

    @Schema(description = "Profile picture URL", example = "http://192.168.1.120:8081/uploads/pictures/profile.jpg")
    private String pictureUrl;

    @Schema(description = "Indicates if user has couchette", example = "false")
    private Boolean isCouchette;

    @Schema(description = "User address")
    private AddressDTO address;

    @Schema(description = "Driver license number", example = "12AB34567")
    private String driverLicenseNumber;

    @Schema(description = "Heures mensuelles du contrat", example = "151.67")
    private Double heureContrat;

    @Schema(description = "User notification preferences")
    private NotificationPreferencesDTO notificationPreferences;
}
