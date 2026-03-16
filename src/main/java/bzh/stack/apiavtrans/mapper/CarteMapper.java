package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.carte.CarteDTO;
import bzh.stack.apiavtrans.entity.Carte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarteMapper {

    private final UserMapper userMapper;
    private final TypeCarteMapper typeCarteMapper;

    public CarteDTO toDTO(Carte carte) {
        if (carte == null) {
            return null;
        }

        CarteDTO dto = new CarteDTO();
        dto.setUuid(carte.getUuid());
        dto.setNom(carte.getNom());
        dto.setDescription(carte.getDescription());
        dto.setCode(carte.getCode());
        dto.setNumero(carte.getNumero());
        dto.setDateExpiration(carte.getDateExpiration());
        dto.setCreatedAt(carte.getCreatedAt());
        dto.setUpdatedAt(carte.getUpdatedAt());

        if (carte.getUser() != null) {
            dto.setUserUuid(carte.getUser().getUuid());
            dto.setUser(userMapper.toDTO(carte.getUser()));
        }

        if (carte.getTypeCarte() != null) {
            dto.setTypeCarteUuid(carte.getTypeCarte().getUuid());
            dto.setTypeCarte(typeCarteMapper.toDTO(carte.getTypeCarte()));
        }

        return dto;
    }
}
