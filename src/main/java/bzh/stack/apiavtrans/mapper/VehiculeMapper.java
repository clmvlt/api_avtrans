package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeDTO;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VehiculeMapper {

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseApiUrl;

    public VehiculeDTO toDTO(Vehicule vehicule, VehiculeKilometrage latestKm) {
        if (vehicule == null) {
            return null;
        }

        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(vehicule.getId());
        dto.setImmat(vehicule.getImmat());
        dto.setCreatedAt(vehicule.getCreatedAt());
        dto.setModel(vehicule.getModel());
        dto.setBrand(vehicule.getBrand());
        dto.setComment(vehicule.getComment());

        if (latestKm != null) {
            dto.setLatestKm(latestKm.getKm());
            dto.setLatestKmDate(latestKm.getCreatedAt());
        }

        if (vehicule.getPicturePath() != null) {
            dto.setPictureUrl(baseApiUrl + "/uploads/vehicules/profile/" + vehicule.getPicturePath());
        }

        return dto;
    }

    public Vehicule toEntity(VehiculeDTO dto) {
        if (dto == null) {
            return null;
        }

        Vehicule vehicule = new Vehicule();
        vehicule.setId(dto.getId());
        vehicule.setImmat(dto.getImmat());
        vehicule.setCreatedAt(dto.getCreatedAt());
        vehicule.setModel(dto.getModel());
        vehicule.setBrand(dto.getBrand());
        vehicule.setComment(dto.getComment());

        return vehicule;
    }
}
