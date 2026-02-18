package bzh.stack.apiavtrans.dto.signature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResponse {
    private Boolean success;
    private String message;
    private SignatureDTO signature;
}
