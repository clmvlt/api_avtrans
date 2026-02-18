package bzh.stack.apiavtrans.dto.notification;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID uuid;
    private UserDTO user;
    private String title;
    private String description;
    private ZonedDateTime createdAt;
    private Boolean isRead;
    private String refType;
    private String refId;
}
