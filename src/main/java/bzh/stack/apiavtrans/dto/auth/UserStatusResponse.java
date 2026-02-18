package bzh.stack.apiavtrans.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusResponse {
    private Boolean isMailVerified;
    private Boolean isActive;
}
