package bzh.stack.apiavtrans.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDTO {
    private UUID uuid;
    private String email;
    private String token;
    private ZonedDateTime verificationDateTime;
    private UUID userUuid;
}
