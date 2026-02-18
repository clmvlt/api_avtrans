package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.PasswordResetToken;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.PasswordResetTokenRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public PasswordResetToken createResetToken(String email) {
        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec cet email : " + normalizedEmail));

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(normalizedEmail);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(ZonedDateTime.now(ZoneId.of("Europe/Paris")).plusHours(1));
        token.setIsUsed(false);
        token.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        token.setUser(user);

        return passwordResetTokenRepository.save(token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de réinitialisation invalide : " + token));

        if (resetToken.getIsUsed()) {
            throw new RuntimeException("Ce token de réinitialisation a déjà été utilisé");
        }

        if (ZonedDateTime.now(ZoneId.of("Europe/Paris")).isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("Le token de réinitialisation a expiré");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(hashPassword(newPassword));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String hashPassword(String password) {
        try {
            java.security.SecureRandom random = new java.security.SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return java.util.Base64.getEncoder().encodeToString(combined);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hashage du mot de passe", e);
        }
    }
}
