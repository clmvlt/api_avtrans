package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.dto.notification.NotificationDTO;
import bzh.stack.apiavtrans.dto.notification.NotificationListResponse;
import bzh.stack.apiavtrans.dto.notification.NotificationResponse;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "[UTILISATEUR] Get all notifications")
    @ApiResponse(
            responseCode = "200",
            description = "Notifications retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationListResponse.class)
            )
    )
    @RequireRole("Utilisateur")
    @GetMapping
    public ResponseEntity<?> getAllNotifications(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            List<NotificationDTO> notifications = notificationService.getAllNotificationsForUser(user);

            return ResponseEntity.ok(new NotificationListResponse(
                    true,
                    "Notifications retrieved successfully",
                    notifications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get unread notifications")
    @ApiResponse(
            responseCode = "200",
            description = "Unread notifications retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationListResponse.class)
            )
    )
    @RequireRole("Utilisateur")
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            List<NotificationDTO> notifications = notificationService.getUnreadNotificationsForUser(user);

            return ResponseEntity.ok(new NotificationListResponse(
                    true,
                    "Unread notifications retrieved successfully",
                    notifications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get read notifications")
    @ApiResponse(
            responseCode = "200",
            description = "Read notifications retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationListResponse.class)
            )
    )
    @RequireRole("Utilisateur")
    @GetMapping("/read")
    public ResponseEntity<?> getReadNotifications(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            List<NotificationDTO> notifications = notificationService.getReadNotificationsForUser(user);

            return ResponseEntity.ok(new NotificationListResponse(
                    true,
                    "Read notifications retrieved successfully",
                    notifications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get unread notifications count")
    @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    @RequireRole("Utilisateur")
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            Long count = notificationService.getUnreadCount(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Unread count retrieved successfully");
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get notification by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Notification retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Notification not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to access this notification",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @RequireRole("Utilisateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getNotificationById(
            @Parameter(description = "Notification UUID") @PathVariable UUID uuid,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            NotificationDTO notification = notificationService.getNotificationById(uuid, user);

            return ResponseEntity.ok(new NotificationResponse(
                    true,
                    "Notification retrieved successfully",
                    notification
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(new ErrorResponse(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Mark notification as read")
    @ApiResponse(
            responseCode = "200",
            description = "Notification marked as read successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Notification not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to modify this notification",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @RequireRole("Utilisateur")
    @PatchMapping("/{uuid}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "Notification UUID") @PathVariable UUID uuid,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            NotificationDTO notification = notificationService.markAsRead(uuid, user);

            return ResponseEntity.ok(new NotificationResponse(
                    true,
                    "Notification marked as read",
                    notification
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(new ErrorResponse(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Mark all notifications as read")
    @ApiResponse(
            responseCode = "200",
            description = "All notifications marked as read successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SuccessMessageResponse.class)
            )
    )
    @RequireRole("Utilisateur")
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            notificationService.markAllAsRead(user);

            return ResponseEntity.ok(new SuccessMessageResponse(
                    true,
                    "All notifications marked as read"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Send notification to all users of a role")
    @ApiResponse(
            responseCode = "200",
            description = "Notifications sent successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationListResponse.class)
            )
    )
    @RequireRole("Administrateur")
    @PostMapping("/send-to-role/{roleName}")
    public ResponseEntity<?> sendNotificationToRole(
            @Parameter(description = "Role name (Administrateur, Mécanicien, Utilisateur)")
            @PathVariable String roleName,
            @Valid @RequestBody NotificationCreateRequest request) {
        try {
            List<NotificationDTO> notifications = notificationService.sendNotificationToRole(roleName, request);

            return ResponseEntity.ok(new NotificationListResponse(
                    true,
                    "Notifications sent to all users with role: " + roleName,
                    notifications
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
