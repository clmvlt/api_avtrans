package bzh.stack.apiavtrans.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersHoursListResponse {
    private Boolean success;
    private String message;
    private List<UserWithHoursDTO> users;
}
