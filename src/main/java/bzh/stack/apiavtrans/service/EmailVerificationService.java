package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.EmailVerification;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.EmailVerificationRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    /**
     * Enregistrer une nouvelle vérification d'email
     */
    @Transactional
    public EmailVerification createVerification(String email) {
        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec cet email : " + normalizedEmail));

        EmailVerification verification = new EmailVerification();
        verification.setEmail(normalizedEmail);
        verification.setToken(UUID.randomUUID().toString());
        verification.setVerificationDateTime(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        verification.setUser(user);

        return emailVerificationRepository.save(verification);
    }

    /**
     * Créer une vérification directement pour un utilisateur
     */
    @Transactional
    public EmailVerification createVerificationForUser(User user) {
        EmailVerification verification = new EmailVerification();
        verification.setEmail(user.getEmail().toLowerCase());
        verification.setToken(UUID.randomUUID().toString());
        verification.setVerificationDateTime(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        verification.setUser(user);

        return emailVerificationRepository.save(verification);
    }

    /**
     * Supprimer toutes les vérifications en cours pour un utilisateur
     */
    @Transactional
    public void deleteVerificationsForUser(User user) {
        emailVerificationRepository.deleteByUser(user);
    }

    /**
     * Valider une vérification d'email avec le token
     */
    @Transactional
    public User validateVerification(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de vérification invalide : " + token));

        User user = verification.getUser();
        user.setIsMailVerified(true);
        userRepository.save(user);

        // Optionnel : supprimer la vérification après validation
        emailVerificationRepository.delete(verification);

        return user;
    }
}
