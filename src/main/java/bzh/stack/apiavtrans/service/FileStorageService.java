package bzh.stack.apiavtrans.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.vehicules-dir:uploads/vehicules}")
    private String vehiculesDir;

    @Value("${app.upload.vehicules-profile-dir:uploads/vehicules/profile}")
    private String vehiculesProfileDir;

    @Value("${app.upload.vehicules-adjust-dir:uploads/vehicules/adjust}")
    private String vehiculesAdjustDir;

    @Value("${app.upload.vehicule-files-dir:uploads/vehicule-files}")
    private String vehiculeFilesDir;

    @Value("${app.upload.entretiens-dir:uploads/entretiens}")
    private String entretiensDir;

    @Value("${app.upload.rapports-dir:uploads/rapports}")
    private String rapportsDir;

    @Value("${app.upload.apk-dir:uploads/apk}")
    private String apkDir;

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseUrl;

    public String saveVehiculeImage(String base64Image, UUID vehiculeId) {
        String fileName = saveBase64Image(base64Image, vehiculeId, vehiculesDir);
        return baseUrl + "/uploads/vehicules/" + fileName;
    }

    public String saveVehiculeProfileImage(String base64Image, UUID vehiculeId) {
        String fileName = saveBase64Image(base64Image, vehiculeId, vehiculesProfileDir);
        return baseUrl + "/uploads/vehicules/profile/" + fileName;
    }

    public void deleteVehiculeProfileImage(String fileName) {
        deleteImage(fileName, vehiculesProfileDir);
    }

    public String saveVehiculeAdjustImage(String base64Image, UUID adjustInfoId) {
        String fileName = saveBase64Image(base64Image, adjustInfoId, vehiculesAdjustDir);
        return baseUrl + "/uploads/vehicules/adjust/" + fileName;
    }

    private String saveBase64Image(String base64Image, UUID entityId, String directory) {
        try {
            String base64Data = base64Image;
            String extension = "png";

            if (base64Image.contains(",")) {
                String[] parts = base64Image.split(",");
                String header = parts[0];
                base64Data = parts[1];

                if (header.contains("jpeg") || header.contains("jpg")) {
                    extension = "jpg";
                } else if (header.contains("gif")) {
                    extension = "gif";
                } else if (header.contains("webp")) {
                    extension = "webp";
                }
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            Path uploadPath = Paths.get(directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = entityId.toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, imageBytes);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 image data", e);
        }
    }

    public void deleteVehiculeImage(String fileName) {
        deleteImage(fileName, vehiculesDir);
    }

    public void deleteVehiculeAdjustImage(String fileName) {
        deleteImage(fileName, vehiculesAdjustDir);
    }

    public String saveRapportImage(String base64Image, UUID rapportId) {
        String fileName = saveBase64Image(base64Image, rapportId, rapportsDir);
        return baseUrl + "/uploads/rapports/" + fileName;
    }

    public void deleteRapportImage(String fileName) {
        deleteImage(fileName, rapportsDir);
    }

    public FileInfo saveEntretienFile(String base64Content, String originalName, String mimeType) {
        try {
            String base64Data = base64Content;
            if (base64Content.contains(",")) {
                base64Data = base64Content.split(",")[1];
            }

            byte[] fileBytes = Base64.getDecoder().decode(base64Data);

            Path uploadPath = Paths.get(entretiensDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension = getExtensionFromMimeType(mimeType, originalName);
            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, fileBytes);

            return new FileInfo(fileName, (long) fileBytes.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 file data", e);
        }
    }

    public void deleteEntretienFile(String fileName) {
        deleteImage(fileName, entretiensDir);
    }

    public FileInfo saveVehiculeFile(String base64Content, String originalName, String mimeType) {
        try {
            String base64Data = base64Content;
            if (base64Content.contains(",")) {
                base64Data = base64Content.split(",")[1];
            }

            byte[] fileBytes = Base64.getDecoder().decode(base64Data);

            Path uploadPath = Paths.get(vehiculeFilesDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension = getExtensionFromMimeType(mimeType, originalName);
            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, fileBytes);

            return new FileInfo(fileName, (long) fileBytes.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 file data", e);
        }
    }

    public void deleteVehiculeFile(String fileName) {
        deleteImage(fileName, vehiculeFilesDir);
    }

    private String getExtensionFromMimeType(String mimeType, String originalName) {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }

        if (mimeType == null) {
            return "bin";
        }

        return switch (mimeType.toLowerCase()) {
            case "application/pdf" -> "pdf";
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "application/msword" -> "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/vnd.ms-excel" -> "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "text/plain" -> "txt";
            case "text/csv" -> "csv";
            default -> "bin";
        };
    }

    @Deprecated
    public String saveBase64File(String base64Image, String subdirectory) {
        UUID randomId = UUID.randomUUID();
        String directory = subdirectory.equals("entretiens") ? entretiensDir : vehiculesDir;
        return saveBase64Image(base64Image, randomId, directory);
    }

    @Deprecated
    public void deleteFile(String fileName, String subdirectory) {
        String directory = subdirectory.equals("entretiens") ? entretiensDir : vehiculesDir;
        deleteImage(fileName, directory);
    }

    private void deleteImage(String fileName, String directory) {
        try {
            Path filePath = Paths.get(directory).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Ignore deletion errors
        }
    }

    public String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public FileInfo saveApkFile(String base64Content, Integer versionCode) {
        try {
            String base64Data = base64Content;
            if (base64Content.contains(",")) {
                base64Data = base64Content.split(",")[1];
            }

            byte[] fileBytes = Base64.getDecoder().decode(base64Data);

            Path uploadPath = Paths.get(apkDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = versionCode + "_" + System.currentTimeMillis() + ".apk";
            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, fileBytes);

            return new FileInfo(fileName, (long) fileBytes.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save APK file", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 APK data", e);
        }
    }

    public void deleteApkFile(String fileName) {
        deleteImage(fileName, apkDir);
    }

    public Path getApkFilePath(String fileName) {
        return Paths.get(apkDir).resolve(fileName);
    }

    public record FileInfo(String fileName, Long fileSize) {}
}
