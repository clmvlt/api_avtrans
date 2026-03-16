package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.*;
import bzh.stack.apiavtrans.dto.notification.NotificationPreferencesDTO;
import bzh.stack.apiavtrans.dto.notification.UpdateNotificationPreferencesRequest;
import bzh.stack.apiavtrans.dto.vehicule.UserLastKilometrageDTO;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(
            summary = "[ADMINISTRATEUR] Get all users with status",
            description = """
                    Returns all users with their current presence status:
                    - **PRESENT**: User has an active service (not on break)
                    - **ON_BREAK**: User is on break
                    - **ABSENT**: User has no active service
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsersWithStatusOnly();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get user by UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @RequireRole("Administrateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID uuid) {
        try {
            User user = userService.getUser(uuid)
                    .orElseThrow(() -> new RuntimeException("User not found with uuid: " + uuid));

            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Update user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @RequireRole("Administrateur")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserRequest.class)
                    )
            )
            @RequestBody UpdateUserRequest request) {
        try {
            User updatedUser = userService.updateUserAdmin(
                    uuid,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getIsActive(),
                    request.getRoleUuid(),
                    request.getIsCouchette(),
                    request.getAddress(),
                    request.getDriverLicenseNumber(),
                    request.getHeureContrat()
            );

            UserDTO userDTO = userMapper.toDTO(updatedUser);
            return ResponseEntity.ok(new UserResponse(true, "Utilisateur mis à jour avec succès", userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @RequireRole("Administrateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID uuid) {
        try {
            userService.deleteUser(uuid);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get all users with presence status and hours worked today",
            description = """
                    Returns all users with their current status and worked hours:
                    - **PRESENT**: User has an active service (not on break)
                    - **ON_BREAK**: User is on break
                    - **ABSENT**: User has no active service

                    For present or on-break users, includes active service details.
                    Also includes hours worked today (hoursToday) for each user.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users with status retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserWithStatusDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping("/status")
    public ResponseEntity<?> getAllUsersWithStatus() {
        try {
            List<UserWithStatusDTO> users = userService.getAllUsersWithStatus();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get all users with worked hours",
            description = "Returns all users with their worked hours for today, current week, current month, current year, and last month"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users with hours retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsersHoursListResponse.class)
            )
    )
    @RequireRole("Administrateur")
    @GetMapping("/hours")
    public ResponseEntity<?> getAllUsersWithHours() {
        try {
            return ResponseEntity.ok(userService.getAllUsersWithHours());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Comparaison heures contrat vs effectuées pour tous les utilisateurs",
            description = "Retourne pour chaque utilisateur la comparaison entre ses heures de contrat et ses heures effectuées sur le mois donné. Par défaut : mois en cours."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Comparaisons récupérées avec succès",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsersContractComparisonListResponse.class)
            )
    )
    @RequireRole("Administrateur")
    @GetMapping("/contract-hours")
    public ResponseEntity<?> getAllUsersContractComparison(
            @Parameter(description = "Année (défaut: année en cours)") @RequestParam(required = false) Integer year,
            @Parameter(description = "Mois 1-12 (défaut: mois en cours)") @RequestParam(required = false) Integer month) {
        try {
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Europe/Paris"));
            int y = year != null ? year : now.getYear();
            int m = month != null ? month : now.getMonthValue();

            var comparisons = userService.getAllUsersContractComparison(y, m);
            return ResponseEntity.ok(new UsersContractComparisonListResponse(true,
                    "Comparaisons contrat récupérées avec succès", y, m, comparisons));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Comparaison heures contrat vs effectuées pour un utilisateur",
            description = "Retourne la comparaison détaillée pour un utilisateur spécifique sur le mois donné."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comparaison récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserContractComparisonResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @RequireRole("Administrateur")
    @GetMapping("/{uuid}/contract-hours")
    public ResponseEntity<?> getUserContractComparison(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID uuid,
            @Parameter(description = "Année (défaut: année en cours)") @RequestParam(required = false) Integer year,
            @Parameter(description = "Mois 1-12 (défaut: mois en cours)") @RequestParam(required = false) Integer month) {
        try {
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Europe/Paris"));
            int y = year != null ? year : now.getYear();
            int m = month != null ? month : now.getMonthValue();

            var comparison = userService.getUserContractComparison(uuid, y, m);
            return ResponseEntity.ok(new UserContractComparisonResponse(true,
                    "Comparaison contrat récupérée avec succès", comparison));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get last vehicle used by each user",
            description = "Returns an array with the last vehicle (from kilometrage entries) used by each user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Last vehicles retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserLastVehicleDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping("/last-vehicles")
    public ResponseEntity<?> getAllUsersLastVehicles() {
        try {
            List<UserLastVehicleDTO> result = userService.getAllUsersLastVehicles();
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get current user's last kilometrage entry",
            description = "Returns the last kilometrage entry by the authenticated user and whether they have already entered a kilometrage today"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Last kilometrage retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLastKilometrageDTO.class)
            )
    )
    @RequireRole("Utilisateur")
    @GetMapping("/me/kilometrage")
    public ResponseEntity<?> getCurrentUserLastKilometrage(HttpServletRequest request) {
        try {
            User user = (User) request.getAttribute("user");
            UserLastKilometrageDTO result = userService.getUserLastKilometrage(user);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get current user's notification preferences")
    @ApiResponse(
            responseCode = "200",
            description = "Notification preferences retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationPreferencesDTO.class)
            )
    )
    @RequireRole("Utilisateur")
    @GetMapping("/me/notification-preferences")
    public ResponseEntity<?> getNotificationPreferences(HttpServletRequest request) {
        try {
            User user = (User) request.getAttribute("user");
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO.getNotificationPreferences());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Update current user's notification preferences")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification preferences updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationPreferencesDTO.class)
                    )
            )
    })
    @RequireRole("Utilisateur")
    @PutMapping("/me/notification-preferences")
    public ResponseEntity<?> updateNotificationPreferences(
            HttpServletRequest request,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Notification preferences to update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateNotificationPreferencesRequest.class)
                    )
            )
            @RequestBody UpdateNotificationPreferencesRequest preferencesRequest) {
        try {
            User user = (User) request.getAttribute("user");
            User updatedUser = userService.updateNotificationPreferences(user, preferencesRequest);
            UserDTO userDTO = userMapper.toDTO(updatedUser);
            return ResponseEntity.ok(userDTO.getNotificationPreferences());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Resend verification email",
            description = "Updates user email address (normalized to lowercase), sets isMailVerified to false, and sends a new verification email. Email is sent first - if sending fails, no changes are saved."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification email sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Email already used by another user or email sending failed")
    })
    @RequireRole("Administrateur")
    @PostMapping("/{uuid}/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New email address for the user",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResendVerificationRequest.class)
                    )
            )
            @RequestBody ResendVerificationRequest request) {
        try {
            User updatedUser = userService.updateEmailAndSendVerification(uuid, request.getEmail());

            UserDTO userDTO = userMapper.toDTO(updatedUser);
            return ResponseEntity.ok(new UserResponse(true, "Email de vérification envoyé avec succès", userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
