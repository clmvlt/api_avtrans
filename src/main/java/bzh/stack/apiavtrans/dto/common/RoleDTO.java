package bzh.stack.apiavtrans.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private UUID uuid;
    private String nom;
    private String color;
}
