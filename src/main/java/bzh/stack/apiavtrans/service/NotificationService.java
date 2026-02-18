package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.dto.notification.NotificationDTO;
import bzh.stack.apiavtrans.entity.Notification;
import bzh.stack.apiavtrans.entity.NotificationPreference;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.NotificationMapper;
import bzh.stack.apiavtrans.repository.NotificationRepository;
import bzh.stack.apiavtrans.repository.RoleRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    @Transactional
    public NotificationDTO createNotification(User user, NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setDescription(request.getDescription());
        notification.setIsRead(false);
        notification.setRefType(request.getRefType());
        notification.setRefId(request.getRefId());

        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toDTO(saved);
    }

    @Transactional
    public List<NotificationDTO> sendNotificationToRole(String roleName, NotificationCreateRequest request) {
        roleRepository.findByNom(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        List<User> usersWithRole = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().getNom().equals(roleName))
                .collect(Collectors.toList());

        List<NotificationDTO> createdNotifications = new ArrayList<>();
        for (User user : usersWithRole) {
            NotificationDTO notificationDTO = createNotification(user, request);
            createdNotifications.add(notificationDTO);
        }

        return createdNotifications;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotificationsForUser(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsForUser(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getReadNotificationsForUser(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, true);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(UUID uuid, User user) {
        Notification notification = notificationRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getUuid().equals(user.getUuid())) {
            throw new SecurityException("You are not authorized to access this notification");
        }

        return notificationMapper.toDTO(notification);
    }

    @Transactional
    public NotificationDTO markAsRead(UUID uuid, User user) {
        Notification notification = notificationRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getUuid().equals(user.getUuid())) {
            throw new SecurityException("You are not authorized to modify this notification");
        }

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toDTO(updated);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Transactional
    public void sendNotificationWithPreference(User user, NotificationCreateRequest request, String notificationType) {
        NotificationPreference pref = getUserPreferenceForType(user, notificationType);

        if (pref == NotificationPreference.NONE) {
            return;
        }

        createNotification(user, request);

        if (pref == NotificationPreference.EMAIL) {
            try {
                emailService.sendNotificationEmail(user.getEmail(), request.getTitle(), request.getDescription());
            } catch (Exception e) {
                log.error("Failed to send notification email to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    @Transactional
    public void sendNotificationToRoleWithPreference(String roleName, NotificationCreateRequest request, String notificationType) {
        sendNotificationToRoleWithPreferenceExcluding(roleName, request, notificationType, null);
    }

    @Transactional
    public void sendNotificationToRoleWithPreferenceExcluding(String roleName, NotificationCreateRequest request, String notificationType, UUID excludeUserUuid) {
        roleRepository.findByNom(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        List<User> usersWithRole = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().getNom().equals(roleName))
                .filter(user -> excludeUserUuid == null || !user.getUuid().equals(excludeUserUuid))
                .collect(Collectors.toList());

        for (User user : usersWithRole) {
            sendNotificationWithPreference(user, request, notificationType);
        }
    }

    private NotificationPreference getUserPreferenceForType(User user, String notificationType) {
        NotificationPreference pref = switch (notificationType) {
            case "acompte" -> user.getNotifPrefAcompte();
            case "absence" -> user.getNotifPrefAbsence();
            case "user_created" -> user.getNotifPrefUserCreated();
            case "rapport_vehicule" -> user.getNotifPrefRapportVehicule();
            case "todo" -> user.getNotifPrefTodo();
            default -> null;
        };
        return pref != null ? pref : NotificationPreference.NONE;
    }
}
