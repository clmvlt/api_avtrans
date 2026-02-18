package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.absence.AbsenceTypeDTO;
import bzh.stack.apiavtrans.entity.AbsenceType;
import org.springframework.stereotype.Component;

@Component
public class AbsenceTypeMapper {

    public AbsenceTypeDTO toDTO(AbsenceType absenceType) {
        if (absenceType == null) {
            return null;
        }

        AbsenceTypeDTO dto = new AbsenceTypeDTO();
        dto.setUuid(absenceType.getUuid());
        dto.setName(absenceType.getName());
        dto.setColor(absenceType.getColor());
        dto.setCreatedAt(absenceType.getCreatedAt());

        return dto;
    }
}
