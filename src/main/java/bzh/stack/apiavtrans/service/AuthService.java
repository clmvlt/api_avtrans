package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.auth.GoogleProfileDTO;
import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.entity.EmailVerification;
import bzh.stack.apiavtrans.entity.Role;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.EmailVerificationRepository;
import bzh.stack.apiavtrans.repository.RoleRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final FileStorageService fileStorageService;

    /**
     * Enregistrer un nouvel utilisateur.
     * L'email de vérification est envoyé AVANT la sauvegarde finale - si l'envoi échoue, rien n'est sauvegardé.
     */
    @Transactional
    public User register(String email, String password, String firstName, String lastName) {
        String normalizedEmail = email.toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("Cet email existe déjà : " + normalizedEmail);
        }

        String passwordHash = hashPassword(password);
        String token = generateApiToken();

        Role utilisateurRole = roleRepository.findByNom("Utilisateur")
                .orElseThrow(() -> new RuntimeException("Le rôle 'Utilisateur' n'a pas été trouvé"));

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIsActive(false);
        user.setIsMailVerified(false);
        user.setRole(utilisateurRole);
        user.setToken(token);

        // Create verification token
        String verificationToken = UUID.randomUUID().toString();

        // Send verification email BEFORE saving - if it fails, nothing is saved
        emailService.sendVerificationEmail(normalizedEmail, verificationToken);

        // Email sent successfully, now save the user
        User savedUser = userRepository.save(user);

        // Save verification record
        EmailVerification verification = new EmailVerification();
        verification.setEmail(normalizedEmail);
        verification.setToken(verificationToken);
        verification.setVerificationDateTime(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        verification.setUser(savedUser);
        emailVerificationRepository.save(verification);

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle("Nouvel utilisateur inscrit");
        notificationRequest.setDescription(String.format("%s %s vient de s'inscrire",
            savedUser.getFirstName(), savedUser.getLastName()));
        notificationRequest.setRefType("user");
        notificationRequest.setRefId(savedUser.getUuid().toString());

        notificationService.sendNotificationToRoleWithPreference("Administrateur", notificationRequest, "user_created");

        return savedUser;
    }

    public User login(String email, String password) {
        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe invalide"));

        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new RuntimeException("Email ou mot de passe invalide");
        }

        if (!user.getIsMailVerified()) {
            throw new RuntimeException("L'adresse email n'a pas été vérifiée");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Le compte n'est pas activé");
        }

        return user;
    }

    /**
     * Tentative de connexion via Google (Sign in with Google).
     * <p>
     * Le frontend transmet l'ID token Google ; on le vérifie, puis :
     * <ul>
     *   <li>si un compte existe déjà pour cet email → on le lie au compte Google et on connecte
     *       l'utilisateur (l'email est considéré vérifié puisque Google l'a vérifié). La connexion
     *       est refusée tant que le compte n'est pas activé par un administrateur ;</li>
     *   <li>sinon → aucun compte n'est créé : on retourne le profil Google afin que le frontend
     *       propose une page de création de compte pré-remplie (voir {@link #googleRegister}).</li>
     * </ul>
     */
    @Transactional
    public GoogleLoginResult googleLogin(String idToken) {
        GoogleIdToken.Payload payload = verifyGooglePayload(idToken);

        String email = payload.getEmail().toLowerCase();
        String googleId = payload.getSubject();

        java.util.Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();

            // Lier le compte Google si ce n'est pas déjà fait
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
            // Google a vérifié l'email : on le considère vérifié
            if (!Boolean.TRUE.equals(user.getIsMailVerified())) {
                user.setIsMailVerified(true);
            }
            // Garantir la présence d'un token d'API
            if (user.getToken() == null) {
                user.setToken(generateApiToken());
            }
            // Récupérer la photo Google si l'utilisateur n'en a pas encore (non bloquant)
            if (user.getPicturePath() == null) {
                String picturePath = fileStorageService.savePictureFromUrl(
                        (String) payload.get("picture"), user.getUuid());
                if (picturePath != null) {
                    user.setPicturePath(picturePath);
                }
            }
            userRepository.save(user);

            if (!user.getIsActive()) {
                throw new RuntimeException("Le compte n'est pas activé");
            }

            return GoogleLoginResult.authenticated(user);
        }

        // Aucun compte : on renvoie le profil Google pour pré-remplir la création
        GoogleProfileDTO profile = new GoogleProfileDTO(
                email,
                (String) payload.get("given_name"),
                (String) payload.get("family_name"),
                (String) payload.get("picture")
        );
        return GoogleLoginResult.needsRegistration(profile);
    }

    /**
     * Création effective d'un compte via Google, après confirmation de l'utilisateur sur la
     * page d'inscription pré-remplie. L'email et l'identifiant Google sont extraits du token
     * (source de confiance) ; le prénom et le nom proviennent du formulaire (modifiables).
     * <p>
     * Le compte est créé avec email vérifié mais inactif : il doit être activé par un
     * administrateur avant la première connexion (une notification leur est envoyée).
     */
    @Transactional
    public User googleRegister(String idToken, String firstName, String lastName) {
        GoogleIdToken.Payload payload = verifyGooglePayload(idToken);

        String email = payload.getEmail().toLowerCase();
        String googleId = payload.getSubject();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Un compte existe déjà pour cet email : " + email);
        }

        // Prénom/nom du formulaire, avec repli sur les valeurs Google si vides
        String resolvedFirstName = (firstName != null && !firstName.isBlank())
                ? firstName.trim() : (String) payload.get("given_name");
        String resolvedLastName = (lastName != null && !lastName.isBlank())
                ? lastName.trim() : (String) payload.get("family_name");

        Role utilisateurRole = roleRepository.findByNom("Utilisateur")
                .orElseThrow(() -> new RuntimeException("Le rôle 'Utilisateur' n'a pas été trouvé"));

        User user = new User();
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setFirstName(resolvedFirstName);
        user.setLastName(resolvedLastName);
        // Compte Google sans mot de passe local : on stocke un hash aléatoire inutilisable
        // (l'utilisateur pourra définir un mot de passe via la réinitialisation s'il le souhaite)
        user.setPasswordHash(hashPassword(generateApiToken()));
        user.setIsMailVerified(true);   // vérifié par Google
        user.setIsActive(false);        // activation par un administrateur requise
        user.setRole(utilisateurRole);
        user.setToken(generateApiToken());

        User savedUser = userRepository.save(user);

        // Récupération automatique de la photo de profil Google (non bloquant en cas d'échec)
        String googlePictureUrl = (String) payload.get("picture");
        String picturePath = fileStorageService.savePictureFromUrl(googlePictureUrl, savedUser.getUuid());
        if (picturePath != null) {
            savedUser.setPicturePath(picturePath);
            savedUser = userRepository.save(savedUser);
        }

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle("Nouvel utilisateur inscrit");
        notificationRequest.setDescription(String.format("%s %s vient de s'inscrire via Google",
                savedUser.getFirstName(), savedUser.getLastName()));
        notificationRequest.setRefType("user");
        notificationRequest.setRefId(savedUser.getUuid().toString());

        notificationService.sendNotificationToRoleWithPreference("Administrateur", notificationRequest, "user_created");

        return savedUser;
    }

    /**
     * Vérifie l'ID token Google et s'assure que l'email est vérifié par Google.
     */
    private GoogleIdToken.Payload verifyGooglePayload(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        Boolean emailVerified = payload.getEmailVerified();
        if (emailVerified == null || !emailVerified) {
            throw new RuntimeException("L'adresse email Google n'est pas vérifiée");
        }
        return payload;
    }

    public User getUserByToken(String token) {
        return userRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));
    }

    public java.util.Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public void changePassword(UUID userUuid, String currentPassword, String newPassword) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        if (!verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        String newPasswordHash = hashPassword(newPassword);
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
    }

    /**
     * Hasher un mot de passe avec SHA-256 et un salt
     */
    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hashage du mot de passe", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, salt.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            int hashLength = combined.length - salt.length;
            for (int i = 0; i < hashLength; i++) {
                if (combined[salt.length + i] != hashedPassword[i]) {
                    return false;
                }
            }

            return true;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors de la vérification du mot de passe", e);
        }
    }

    private String generateApiToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
