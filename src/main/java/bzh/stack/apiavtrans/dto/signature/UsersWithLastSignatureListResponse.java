package bzh.stack.apiavtrans.dto.signature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersWithLastSignatureListResponse {
    private Boolean success;
    private String message;
    private List<UserWithLastSignatureDTO> users;
}
