package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.entretien.DossierTypeEntretienDTO;
import bzh.stack.apiavtrans.entity.DossierTypeEntretien;
import org.springframework.stereotype.Component;

@Component
public class DossierTypeEntretienMapper {

    public DossierTypeEntretienDTO toDTO(DossierTypeEntretien dossier) {
        if (dossier == null) {
            return null;
        }

        DossierTypeEntretienDTO dto = new DossierTypeEntretienDTO();
        dto.setId(dossier.getId());
        dto.setNom(dossier.getNom());
        dto.setDescription(dossier.getDescription());
        dto.setCreatedAt(dossier.getCreatedAt());

        return dto;
    }

    public DossierTypeEntretien toEntity(DossierTypeEntretienDTO dto) {
        if (dto == null) {
            return null;
        }

        DossierTypeEntretien dossier = new DossierTypeEntretien();
        dossier.setId(dto.getId());
        dossier.setNom(dto.getNom());
        dossier.setDescription(dto.getDescription());
        dossier.setCreatedAt(dto.getCreatedAt());

        return dossier;
    }
}
