package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.common.RoleDTO;
import bzh.stack.apiavtrans.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    /**
     * Convertit une entité Role en RoleDTO
     */
    public RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setUuid(role.getUuid());
        dto.setNom(role.getNom());
        dto.setColor(role.getColor());

        return dto;
    }

    /**
     * Convertit un RoleDTO en entité Role
     */
    public Role toEntity(RoleDTO dto) {
        if (dto == null) {
            return null;
        }

        Role role = new Role();
        role.setUuid(dto.getUuid());
        role.setNom(dto.getNom());
        role.setColor(dto.getColor());

        return role;
    }
}
