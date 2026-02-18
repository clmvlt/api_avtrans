package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculePictureDTO;
import bzh.stack.apiavtrans.entity.RapportVehiculePicture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RapportVehiculePictureMapper {

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseUrl;

    public RapportVehiculePictureDTO toDTO(RapportVehiculePicture picture) {
        if (picture == null) {
            return null;
        }

        RapportVehiculePictureDTO dto = new RapportVehiculePictureDTO();
        dto.setId(picture.getId());
        dto.setRapportVehiculeId(picture.getRapportVehicule() != null ? picture.getRapportVehicule().getId() : null);
        if (picture.getPicturePath() != null) {
            dto.setPictureUrl(baseUrl + "/uploads/rapports/" + picture.getPicturePath());
        }
        dto.setCreatedAt(picture.getCreatedAt());

        return dto;
    }

    public RapportVehiculePicture toEntity(RapportVehiculePictureDTO dto) {
        if (dto == null) {
            return null;
        }

        RapportVehiculePicture picture = new RapportVehiculePicture();
        picture.setId(dto.getId());
        picture.setCreatedAt(dto.getCreatedAt());

        return picture;
    }
}
