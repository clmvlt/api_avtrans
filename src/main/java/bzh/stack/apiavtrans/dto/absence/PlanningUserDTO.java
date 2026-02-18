package bzh.stack.apiavtrans.dto.absence;

import bzh.stack.apiavtrans.dto.common.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningUserDTO {
    private UUID uuid;
    private String email;
    private String firstName;
    private String lastName;
    private RoleDTO role;
    private String pictureUrl;
    private Boolean isCouchette;
    private List<AbsenceDTO> absences;
}
