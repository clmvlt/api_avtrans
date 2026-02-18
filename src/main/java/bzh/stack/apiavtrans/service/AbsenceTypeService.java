package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.absence.*;
import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.mapper.AbsenceTypeMapper;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.AbsenceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AbsenceTypeService {

    private final AbsenceTypeRepository absenceTypeRepository;
    private final AbsenceTypeMapper absenceTypeMapper;
    private final AbsenceRepository absenceRepository;

    public AbsenceTypeService(AbsenceTypeRepository absenceTypeRepository,
                               AbsenceTypeMapper absenceTypeMapper,
                               AbsenceRepository absenceRepository) {
        this.absenceTypeRepository = absenceTypeRepository;
        this.absenceTypeMapper = absenceTypeMapper;
        this.absenceRepository = absenceRepository;
    }

    public AbsenceTypeListResponse getAllTypes() {
        List<AbsenceType> types = absenceTypeRepository.findAll();
        List<AbsenceTypeDTO> typeDTOs = types.stream()
                .map(absenceTypeMapper::toDTO)
                .collect(Collectors.toList());

        return new AbsenceTypeListResponse(true, typeDTOs);
    }

    @Transactional
    public AbsenceTypeResponse createType(AbsenceTypeCreateRequest request) {
        if (absenceTypeRepository.existsByName(request.getName())) {
            return new AbsenceTypeResponse(false, "Un type avec ce nom existe déjà", null);
        }

        AbsenceType absenceType = new AbsenceType();
        absenceType.setName(request.getName());
        absenceType.setColor(request.getColor());

        AbsenceType saved = absenceTypeRepository.save(absenceType);

        return new AbsenceTypeResponse(true, "Type d'absence créé avec succès", absenceTypeMapper.toDTO(saved));
    }

    @Transactional
    public AbsenceTypeResponse updateType(UUID typeUuid, AbsenceTypeCreateRequest request) {
        AbsenceType absenceType = absenceTypeRepository.findById(typeUuid)
                .orElseThrow(() -> new RuntimeException("Type d'absence non trouvé"));

        if (!absenceType.getName().equals(request.getName()) && absenceTypeRepository.existsByName(request.getName())) {
            return new AbsenceTypeResponse(false, "Un type avec ce nom existe déjà", null);
        }

        absenceType.setName(request.getName());
        if (request.getColor() != null) {
            absenceType.setColor(request.getColor());
        }

        AbsenceType saved = absenceTypeRepository.save(absenceType);

        return new AbsenceTypeResponse(true, "Type d'absence modifié avec succès", absenceTypeMapper.toDTO(saved));
    }

    @Transactional
    public AbsenceTypeResponse deleteType(UUID typeUuid) {
        AbsenceType absenceType = absenceTypeRepository.findById(typeUuid)
                .orElseThrow(() -> new RuntimeException("Type d'absence non trouvé"));

        String deletedTypeName = absenceType.getName();

        if ("Autre".equals(deletedTypeName)) {
            return new AbsenceTypeResponse(false, "Le type 'Autre' ne peut pas être supprimé", absenceTypeMapper.toDTO(absenceType));
        }

        AbsenceType autreType = absenceTypeRepository.findByName("Autre")
                .orElseThrow(() -> new RuntimeException("Type 'Autre' non trouvé"));

        List<Absence> absencesWithType = absenceRepository.findByAbsenceType(absenceType);
        for (Absence absence : absencesWithType) {
            absence.setAbsenceType(autreType);
            absence.setCustomType(deletedTypeName);
            absenceRepository.save(absence);
        }

        absenceTypeRepository.delete(absenceType);

        return new AbsenceTypeResponse(true, "Type d'absence supprimé avec succès", null);
    }

    public AbsenceTypeResponse getTypeById(UUID typeUuid) {
        AbsenceType absenceType = absenceTypeRepository.findById(typeUuid)
                .orElseThrow(() -> new RuntimeException("Type d'absence non trouvé"));

        return new AbsenceTypeResponse(true, null, absenceTypeMapper.toDTO(absenceType));
    }
}
