package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.notification.NotificationDTO;
import bzh.stack.apiavtrans.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setUuid(notification.getUuid());
        dto.setUser(userMapper.toDTO(notification.getUser()));
        dto.setTitle(notification.getTitle());
        dto.setDescription(notification.getDescription());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setIsRead(notification.getIsRead());
        dto.setRefType(notification.getRefType());
        dto.setRefId(notification.getRefId());

        return dto;
    }

    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setUuid(dto.getUuid());
        notification.setUser(userMapper.toEntity(dto.getUser()));
        notification.setTitle(dto.getTitle());
        notification.setDescription(dto.getDescription());
        notification.setCreatedAt(dto.getCreatedAt());
        notification.setIsRead(dto.getIsRead());
        notification.setRefType(dto.getRefType());
        notification.setRefId(dto.getRefId());

        return notification;
    }
}
