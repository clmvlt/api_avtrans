package bzh.stack.apiavtrans.dto.signature;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureDTO {
    private UUID uuid;
    private String signatureBase64;
    private ZonedDateTime date;
    private Double heuresSignees;
    private UserDTO user;
    private ZonedDateTime createdAt;
}
