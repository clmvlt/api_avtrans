package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.absence.AbsenceDTO;
import bzh.stack.apiavtrans.dto.absence.AbsenceDecompteDTO;
import bzh.stack.apiavtrans.dto.absence.JourDecompteDTO;
import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.AbsenceDecompte;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.JourDecompte;
import org.springframework.stereotype.Component;

import java.util.List;

import static bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.round2;

@Component
public class AbsenceMapper {

    private final UserMapper userMapper;
    private final AbsenceTypeMapper absenceTypeMapper;
    private final HeuresAbsenceCalculator heuresAbsenceCalculator;

    public AbsenceMapper(UserMapper userMapper, AbsenceTypeMapper absenceTypeMapper,
                         HeuresAbsenceCalculator heuresAbsenceCalculator) {
        this.userMapper = userMapper;
        this.absenceTypeMapper = absenceTypeMapper;
        this.heuresAbsenceCalculator = heuresAbsenceCalculator;
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

        AbsenceDecompte decompte = heuresAbsenceCalculator.decompter(absence);
        dto.setJoursDecomptes(decompte.joursDecomptes());
        dto.setHeures(round2(decompte.heures()));
        dto.setHeuresCalculees(decompte.heuresCalculees());
        dto.setHeuresForcees(decompte.heuresForcees());
        dto.setModeDecompte(decompte.mode().name());
        dto.setCompteHeures(decompte.compteHeures());
        dto.setContratRenseigne(decompte.contratRenseigne());

        return dto;
    }

    public AbsenceDecompteDTO toDecompteDTO(AbsenceDecompte decompte) {
        if (decompte == null) {
            return null;
        }

        List<JourDecompteDTO> jours = decompte.jours().stream()
                .map(this::toJourDTO)
                .toList();

        AbsenceDecompteDTO dto = new AbsenceDecompteDTO();
        dto.setModeDecompte(decompte.mode().name());
        dto.setCompteHeures(decompte.compteHeures());
        dto.setContratRenseigne(decompte.contratRenseigne());
        dto.setHeureContratMensuel(decompte.heureContratMensuel());
        dto.setHeuresHebdo(decompte.heuresHebdo() != null ? round2(decompte.heuresHebdo()) : null);
        dto.setHeuresParJour(round2(decompte.heuresParJour()));
        dto.setJoursDecomptes(decompte.joursDecomptes());
        dto.setHeuresCalculees(decompte.heuresCalculees());
        dto.setHeuresForcees(decompte.heuresForcees());
        dto.setHeures(round2(decompte.heures()));
        dto.setJours(jours);
        return dto;
    }

    private JourDecompteDTO toJourDTO(JourDecompte jour) {
        return new JourDecompteDTO(
                jour.date(),
                jour.fraction(),
                round2(jour.heures()),
                jour.motif() != null ? jour.motif().name() : null,
                jour.ferie()
        );
    }
}
