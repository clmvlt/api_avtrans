package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeAdjustInfoPictureDTO;
import bzh.stack.apiavtrans.entity.VehiculeAdjustInfoPicture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VehiculeAdjustInfoPictureMapper {

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseUrl;

    public VehiculeAdjustInfoPictureDTO toDTO(VehiculeAdjustInfoPicture picture) {
        if (picture == null) {
            return null;
        }

        VehiculeAdjustInfoPictureDTO dto = new VehiculeAdjustInfoPictureDTO();
        dto.setId(picture.getId());
        dto.setAdjustInfoId(picture.getAdjustInfo() != null ? picture.getAdjustInfo().getId() : null);
        if (picture.getPicturePath() != null) {
            dto.setPictureUrl(baseUrl + "/uploads/vehicules/adjust/" + picture.getPicturePath());
        }
        dto.setCreatedAt(picture.getCreatedAt());

        return dto;
    }

    public VehiculeAdjustInfoPicture toEntity(VehiculeAdjustInfoPictureDTO dto) {
        if (dto == null) {
            return null;
        }

        VehiculeAdjustInfoPicture picture = new VehiculeAdjustInfoPicture();
        picture.setId(dto.getId());
        picture.setCreatedAt(dto.getCreatedAt());

        return picture;
    }
}
