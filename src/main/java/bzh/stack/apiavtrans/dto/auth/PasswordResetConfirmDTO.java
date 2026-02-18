package bzh.stack.apiavtrans.dto.auth;

import lombok.Data;

@Data
public class PasswordResetConfirmDTO {
    private String token;
    private String newPassword;
}
