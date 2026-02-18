package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeKilometrageDTO;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehiculeKilometrageMapper {

    private final UserMapper userMapper;

    public VehiculeKilometrageDTO toDTO(VehiculeKilometrage kilometrage) {
        if (kilometrage == null) {
            return null;
        }

        VehiculeKilometrageDTO dto = new VehiculeKilometrageDTO();
        dto.setId(kilometrage.getId());
        dto.setVehiculeId(kilometrage.getVehicule() != null ? kilometrage.getVehicule().getId() : null);
        dto.setKm(kilometrage.getKm());
        dto.setUser(userMapper.toDTO(kilometrage.getUser()));
        dto.setCreatedAt(kilometrage.getCreatedAt());

        return dto;
    }

    public VehiculeKilometrage toEntity(VehiculeKilometrageDTO dto) {
        if (dto == null) {
            return null;
        }

        VehiculeKilometrage kilometrage = new VehiculeKilometrage();
        kilometrage.setId(dto.getId());
        kilometrage.setKm(dto.getKm());
        kilometrage.setCreatedAt(dto.getCreatedAt());

        return kilometrage;
    }
}
