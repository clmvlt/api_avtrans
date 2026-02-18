package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to resend verification email with updated email address")
public class ResendVerificationRequest {
    @Schema(description = "New email address (will be normalized to lowercase)", example = "user@example.com", required = true)
    private String email;
}
