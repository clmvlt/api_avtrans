package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeFileDTO;
import bzh.stack.apiavtrans.entity.VehiculeFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class VehiculeFileMapper {

    @Value("${app.upload.vehicule-files-dir:uploads/vehicule-files}")
    private String uploadDir;

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseUrl;

    public VehiculeFileDTO toDTO(VehiculeFile file) {
        if (file == null) {
            return null;
        }

        VehiculeFileDTO dto = new VehiculeFileDTO();
        dto.setId(file.getId());
        dto.setVehiculeId(file.getVehicule() != null ? file.getVehicule().getId() : null);
        dto.setOriginalName(file.getOriginalName());
        dto.setMimeType(file.getMimeType());
        dto.setFileSize(file.getFileSize());
        dto.setCreatedAt(file.getCreatedAt());

        if (file.getFilePath() != null) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(uploadDir, file.getFilePath()));
                String base64 = Base64.getEncoder().encodeToString(fileContent);
                dto.setFileB64(base64);
            } catch (IOException e) {
                dto.setFileB64(null);
            }
        }

        return dto;
    }

    public VehiculeFile toEntity(VehiculeFileDTO dto) {
        if (dto == null) {
            return null;
        }

        VehiculeFile file = new VehiculeFile();
        file.setId(dto.getId());
        file.setOriginalName(dto.getOriginalName());
        file.setMimeType(dto.getMimeType());
        file.setFileSize(dto.getFileSize());
        file.setCreatedAt(dto.getCreatedAt());

        return file;
    }

    public VehiculeFileDTO toDTOWithUrl(VehiculeFile file) {
        if (file == null) {
            return null;
        }

        VehiculeFileDTO dto = new VehiculeFileDTO();
        dto.setId(file.getId());
        dto.setVehiculeId(file.getVehicule() != null ? file.getVehicule().getId() : null);
        dto.setOriginalName(file.getOriginalName());
        dto.setMimeType(file.getMimeType());
        dto.setFileSize(file.getFileSize());
        dto.setCreatedAt(file.getCreatedAt());
        if (file.getFilePath() != null) {
            dto.setFileUrl(baseUrl + "/uploads/vehicule-files/" + file.getFilePath());
        }

        return dto;
    }
}
