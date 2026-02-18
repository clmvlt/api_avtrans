package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.entretien.TypeEntretienDTO;
import bzh.stack.apiavtrans.entity.TypeEntretien;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypeEntretienMapper {

    private final DossierTypeEntretienMapper dossierTypeEntretienMapper;

    public TypeEntretienDTO toDTO(TypeEntretien typeEntretien) {
        if (typeEntretien == null) {
            return null;
        }

        TypeEntretienDTO dto = new TypeEntretienDTO();
        dto.setId(typeEntretien.getId());
        dto.setNom(typeEntretien.getNom());
        dto.setDescription(typeEntretien.getDescription());
        dto.setDossier(dossierTypeEntretienMapper.toDTO(typeEntretien.getDossier()));
        dto.setCreatedAt(typeEntretien.getCreatedAt());

        return dto;
    }

    public TypeEntretien toEntity(TypeEntretienDTO dto) {
        if (dto == null) {
            return null;
        }

        TypeEntretien typeEntretien = new TypeEntretien();
        typeEntretien.setId(dto.getId());
        typeEntretien.setNom(dto.getNom());
        typeEntretien.setDescription(dto.getDescription());
        typeEntretien.setDossier(dossierTypeEntretienMapper.toEntity(dto.getDossier()));
        typeEntretien.setCreatedAt(dto.getCreatedAt());

        return typeEntretien;
    }
}
