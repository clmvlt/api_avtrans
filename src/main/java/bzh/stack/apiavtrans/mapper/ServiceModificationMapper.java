package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.service.ServiceModificationDTO;
import bzh.stack.apiavtrans.entity.ServiceModification;
import org.springframework.stereotype.Component;

@Component
public class ServiceModificationMapper {

    private final UserMapper userMapper;

    public ServiceModificationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ServiceModificationDTO toDTO(ServiceModification modification) {
        if (modification == null) {
            return null;
        }

        ServiceModificationDTO dto = new ServiceModificationDTO();
        dto.setUuid(modification.getUuid());
        dto.setServiceUuid(modification.getServiceUuid());
        dto.setAction(modification.getAction() != null ? modification.getAction().name() : null);
        dto.setUser(userMapper.toDTO(modification.getUser()));
        dto.setModifiedBy(userMapper.toDTO(modification.getModifiedBy()));
        dto.setOldDebut(modification.getOldDebut());
        dto.setOldFin(modification.getOldFin());
        dto.setOldIsBreak(modification.getOldIsBreak());
        dto.setNewDebut(modification.getNewDebut());
        dto.setNewFin(modification.getNewFin());
        dto.setNewIsBreak(modification.getNewIsBreak());
        dto.setCreatedAt(modification.getCreatedAt());

        return dto;
    }
}
