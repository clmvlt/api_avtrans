package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.carte.TypeCarteDTO;
import bzh.stack.apiavtrans.entity.TypeCarte;
import org.springframework.stereotype.Component;

@Component
public class TypeCarteMapper {

    public TypeCarteDTO toDTO(TypeCarte typeCarte) {
        if (typeCarte == null) {
            return null;
        }

        TypeCarteDTO dto = new TypeCarteDTO();
        dto.setUuid(typeCarte.getUuid());
        dto.setNom(typeCarte.getNom());
        dto.setDescription(typeCarte.getDescription());
        dto.setCreatedAt(typeCarte.getCreatedAt());
        dto.setUpdatedAt(typeCarte.getUpdatedAt());

        return dto;
    }
}
