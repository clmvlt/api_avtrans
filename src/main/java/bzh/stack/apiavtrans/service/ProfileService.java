package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Value("${app.upload.pictures-dir:uploads/pictures}")
    private String picturesDir;

    @Transactional
    public User updateProfilePicture(UUID userUuid, String base64Image) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        if (user.getPicturePath() != null) {
            deleteOldPicture(user.getPicturePath());
        }

        String fileName = saveBase64Image(base64Image, userUuid);
        user.setPicturePath(fileName);

        return userRepository.save(user);
    }

    private String saveBase64Image(String base64Image, UUID userUuid) {
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

            Path uploadPath = Paths.get(picturesDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = userUuid.toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, imageBytes);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 image data", e);
        }
    }

    private void deleteOldPicture(String fileName) {
        try {
            Path filePath = Paths.get(picturesDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Ignore deletion errors
        }
    }

    @Transactional
    public User updateProfile(UUID userUuid, String firstName, String lastName, String email, String base64Picture) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (email != null) {
            String normalizedEmail = email.toLowerCase();
            if (!normalizedEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(normalizedEmail).isPresent()) {
                    throw new RuntimeException("Email already exists: " + normalizedEmail);
                }
                user.setEmail(normalizedEmail);
            }
        }
        if (base64Picture != null) {
            if (base64Picture.isEmpty()) {
                if (user.getPicturePath() != null) {
                    deleteOldPicture(user.getPicturePath());
                    user.setPicturePath(null);
                }
            } else {
                if (user.getPicturePath() != null) {
                    deleteOldPicture(user.getPicturePath());
                }
                String fileName = saveBase64Image(base64Picture, userUuid);
                user.setPicturePath(fileName);
            }
        }

        return userRepository.save(user);
    }
}
