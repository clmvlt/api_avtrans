package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.auth.AuthUserDTO;
import bzh.stack.apiavtrans.dto.common.UserDTO;
import bzh.stack.apiavtrans.dto.notification.NotificationPreferencesDTO;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseApiUrl;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     * Convertit une entité User en UserDTO
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUuid(user.getUuid());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setIsMailVerified(user.getIsMailVerified());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRole(roleMapper.toDTO(user.getRole()));
        if (user.getPicturePath() != null) {
            dto.setPictureUrl(baseApiUrl + "/uploads/pictures/" + user.getPicturePath());
        }
        dto.setIsCouchette(user.getIsCouchette());
        dto.setNotificationPreferences(mapNotificationPreferences(user));

        return dto;
    }

    /**
     * Convertit une entité User en AuthUserDTO (avec token pour authentification)
     */
    public AuthUserDTO toAuthDTO(User user) {
        if (user == null) {
            return null;
        }

        AuthUserDTO dto = new AuthUserDTO();
        dto.setUuid(user.getUuid());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setIsMailVerified(user.getIsMailVerified());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRole(roleMapper.toDTO(user.getRole()));
        dto.setToken(user.getToken());
        if (user.getPicturePath() != null) {
            dto.setPictureUrl(baseApiUrl + "/uploads/pictures/" + user.getPicturePath());
        }
        dto.setIsCouchette(user.getIsCouchette());
        dto.setNotificationPreferences(mapNotificationPreferences(user));

        return dto;
    }

    private NotificationPreferencesDTO mapNotificationPreferences(User user) {
        NotificationPreferencesDTO prefs = new NotificationPreferencesDTO();
        prefs.setAcompte(user.getNotifPrefAcompte());
        prefs.setAbsence(user.getNotifPrefAbsence());
        prefs.setUserCreated(user.getNotifPrefUserCreated());
        prefs.setRapportVehicule(user.getNotifPrefRapportVehicule());
        prefs.setTodo(user.getNotifPrefTodo());
        return prefs;
    }

    /**
     * Convertit un UserDTO en entité User
     */
    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUuid(dto.getUuid());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setIsMailVerified(dto.getIsMailVerified());
        user.setIsActive(dto.getIsActive());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        user.setRole(roleMapper.toEntity(dto.getRole()));
        user.setIsCouchette(dto.getIsCouchette());

        return user;
    }
}
