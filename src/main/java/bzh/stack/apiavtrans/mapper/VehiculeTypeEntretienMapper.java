package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.entretien.VehiculeTypeEntretienDTO;
import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehiculeTypeEntretienMapper {

    private final TypeEntretienMapper typeEntretienMapper;

    public VehiculeTypeEntretienDTO toDTO(VehiculeTypeEntretien vehiculeTypeEntretien) {
        if (vehiculeTypeEntretien == null) {
            return null;
        }

        VehiculeTypeEntretienDTO dto = new VehiculeTypeEntretienDTO();
        dto.setId(vehiculeTypeEntretien.getId());
        dto.setVehiculeId(vehiculeTypeEntretien.getVehicule() != null ? vehiculeTypeEntretien.getVehicule().getId() : null);
        dto.setVehiculeImmat(vehiculeTypeEntretien.getVehicule() != null ? vehiculeTypeEntretien.getVehicule().getImmat() : null);
        dto.setTypeEntretien(typeEntretienMapper.toDTO(vehiculeTypeEntretien.getTypeEntretien()));
        dto.setPeriodiciteType(vehiculeTypeEntretien.getPeriodiciteType());
        dto.setPeriodiciteValeur(vehiculeTypeEntretien.getPeriodiciteValeur());
        dto.setActif(vehiculeTypeEntretien.getActif());
        dto.setCreatedAt(vehiculeTypeEntretien.getCreatedAt());

        return dto;
    }

    public VehiculeTypeEntretien toEntity(VehiculeTypeEntretienDTO dto) {
        if (dto == null) {
            return null;
        }

        VehiculeTypeEntretien vehiculeTypeEntretien = new VehiculeTypeEntretien();
        vehiculeTypeEntretien.setId(dto.getId());
        vehiculeTypeEntretien.setPeriodiciteType(dto.getPeriodiciteType());
        vehiculeTypeEntretien.setPeriodiciteValeur(dto.getPeriodiciteValeur());
        vehiculeTypeEntretien.setActif(dto.getActif());
        vehiculeTypeEntretien.setCreatedAt(dto.getCreatedAt());

        return vehiculeTypeEntretien;
    }
}
