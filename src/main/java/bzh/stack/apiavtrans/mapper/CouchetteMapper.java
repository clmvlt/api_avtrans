package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.couchette.CouchetteDTO;
import bzh.stack.apiavtrans.entity.Couchette;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouchetteMapper {

    private final UserMapper userMapper;

    public CouchetteDTO toDTO(Couchette couchette) {
        if (couchette == null) {
            return null;
        }

        CouchetteDTO dto = new CouchetteDTO();
        dto.setUuid(couchette.getUuid());
        dto.setDate(couchette.getDate());
        dto.setUser(userMapper.toDTO(couchette.getUser()));
        dto.setCreatedAt(couchette.getCreatedAt());

        return dto;
    }
}
