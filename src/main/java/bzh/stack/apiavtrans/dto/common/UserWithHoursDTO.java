package bzh.stack.apiavtrans.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithHoursDTO {
    private UserDTO user;
    private UserHoursDTO hours;
}
