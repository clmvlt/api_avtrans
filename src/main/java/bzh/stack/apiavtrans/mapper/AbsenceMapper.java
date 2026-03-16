package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.absence.AbsenceDTO;
import bzh.stack.apiavtrans.entity.Absence;
import org.springframework.stereotype.Component;

@Component
public class AbsenceMapper {

    private final UserMapper userMapper;
    private final AbsenceTypeMapper absenceTypeMapper;

    public AbsenceMapper(UserMapper userMapper, AbsenceTypeMapper absenceTypeMapper) {
        this.userMapper = userMapper;
        this.absenceTypeMapper = absenceTypeMapper;
    }

    public AbsenceDTO toDTO(Absence absence) {
        if (absence == null) {
            return null;
        }

        AbsenceDTO dto = new AbsenceDTO();
        dto.setUuid(absence.getUuid());
        dto.setUser(userMapper.toDTO(absence.getUser()));
        dto.setStartDate(absence.getStartDate());
        dto.setEndDate(absence.getEndDate());
        dto.setReason(absence.getReason());
        dto.setAbsenceType(absenceTypeMapper.toDTO(absence.getAbsenceType()));
        dto.setCustomType(absence.getCustomType());
        dto.setPeriod(absence.getPeriod().name());
        dto.setStatus(absence.getStatus().name());
        dto.setValidatedBy(userMapper.toDTO(absence.getValidatedBy()));
        dto.setValidatedAt(absence.getValidatedAt());
        dto.setRejectionReason(absence.getRejectionReason());
        dto.setCreatedAt(absence.getCreatedAt());
        dto.setUpdatedAt(absence.getUpdatedAt());

        return dto;
    }
}
