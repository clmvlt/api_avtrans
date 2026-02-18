package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.entretien.EntretienFileDTO;
import bzh.stack.apiavtrans.entity.EntretienFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class EntretienFileMapper {

    @Value("${app.upload.entretiens-dir:uploads/entretiens}")
    private String uploadDir;

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseUrl;

    public EntretienFileDTO toDTO(EntretienFile file) {
        if (file == null) {
            return null;
        }

        EntretienFileDTO dto = new EntretienFileDTO();
        dto.setId(file.getId());
        dto.setEntretienId(file.getEntretien() != null ? file.getEntretien().getId() : null);
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

    public EntretienFile toEntity(EntretienFileDTO dto) {
        if (dto == null) {
            return null;
        }

        EntretienFile file = new EntretienFile();
        file.setId(dto.getId());
        file.setOriginalName(dto.getOriginalName());
        file.setMimeType(dto.getMimeType());
        file.setFileSize(dto.getFileSize());
        file.setCreatedAt(dto.getCreatedAt());

        return file;
    }

    public EntretienFileDTO toDTOWithUrl(EntretienFile file) {
        if (file == null) {
            return null;
        }

        EntretienFileDTO dto = new EntretienFileDTO();
        dto.setId(file.getId());
        dto.setEntretienId(file.getEntretien() != null ? file.getEntretien().getId() : null);
        dto.setOriginalName(file.getOriginalName());
        dto.setMimeType(file.getMimeType());
        dto.setFileSize(file.getFileSize());
        dto.setCreatedAt(file.getCreatedAt());
        if (file.getFilePath() != null) {
            dto.setFileUrl(baseUrl + "/uploads/entretiens/" + file.getFilePath());
        }

        return dto;
    }
}
