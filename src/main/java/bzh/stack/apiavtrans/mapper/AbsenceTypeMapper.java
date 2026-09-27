package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.absence.AbsenceTypeDTO;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.repository.AbsenceTypeRepository;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator;
import org.springframework.stereotype.Component;

@Component
public class AbsenceTypeMapper {

    private final AbsenceTypeRepository absenceTypeRepository;

    public AbsenceTypeMapper(AbsenceTypeRepository absenceTypeRepository) {
        this.absenceTypeRepository = absenceTypeRepository;
    }

    public AbsenceTypeDTO toDTO(AbsenceType absenceType) {
        if (absenceType == null) {
            return null;
        }

        AbsenceTypeDTO dto = new AbsenceTypeDTO();
        dto.setUuid(absenceType.getUuid());
        dto.setName(absenceType.getName());
        dto.setCreatedAt(absenceType.getCreatedAt());

        String color = absenceType.getColor();
        if (color == null || color.isEmpty()) {
            color = absenceTypeRepository.findFirstByNameIgnoreCaseAndColorIsNotNull(absenceType.getName())
                    .map(AbsenceType::getColor)
                    .orElse(null);
        }
        dto.setColor(color);
        dto.setModeDecompte(HeuresAbsenceCalculator.modeDe(absenceType).name());
        dto.setCompteHeures(HeuresAbsenceCalculator.compteHeuresDe(absenceType));

        return dto;
    }
}
