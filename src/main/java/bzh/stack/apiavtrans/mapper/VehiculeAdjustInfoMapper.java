package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeAdjustInfoDTO;
import bzh.stack.apiavtrans.entity.VehiculeAdjustInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehiculeAdjustInfoMapper {

    private final UserMapper userMapper;

    public VehiculeAdjustInfoDTO toDTO(VehiculeAdjustInfo adjustInfo) {
        if (adjustInfo == null) {
            return null;
        }

        VehiculeAdjustInfoDTO dto = new VehiculeAdjustInfoDTO();
        dto.setId(adjustInfo.getId());
        dto.setVehiculeId(adjustInfo.getVehicule() != null ? adjustInfo.getVehicule().getId() : null);
        dto.setUser(userMapper.toDTO(adjustInfo.getUser()));
        dto.setComment(adjustInfo.getComment());
        dto.setCreatedAt(adjustInfo.getCreatedAt());

        return dto;
    }

    public VehiculeAdjustInfo toEntity(VehiculeAdjustInfoDTO dto) {
        if (dto == null) {
            return null;
        }

        VehiculeAdjustInfo adjustInfo = new VehiculeAdjustInfo();
        adjustInfo.setId(dto.getId());
        adjustInfo.setComment(dto.getComment());
        adjustInfo.setCreatedAt(dto.getCreatedAt());

        return adjustInfo;
    }
}
