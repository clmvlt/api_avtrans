package bzh.stack.apiavtrans.dto.signature;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithLastSignatureDTO {
    private UserDTO user;
    private SignatureDTO lastSignature;
}
