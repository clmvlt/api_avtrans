package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import bzh.stack.apiavtrans.dto.common.UserWithStatusDTO;
import bzh.stack.apiavtrans.dto.common.UserHoursDTO;
import bzh.stack.apiavtrans.dto.common.UserWithHoursDTO;
import bzh.stack.apiavtrans.dto.common.UsersHoursListResponse;
import bzh.stack.apiavtrans.dto.notification.UpdateNotificationPreferencesRequest;
import bzh.stack.apiavtrans.dto.vehicule.UserLastKilometrageDTO;
import bzh.stack.apiavtrans.entity.EmailVerification;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.mapper.VehiculeKilometrageMapper;
import bzh.stack.apiavtrans.entity.Role;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.AcompteRepository;
import bzh.stack.apiavtrans.repository.CouchetteRepository;
import bzh.stack.apiavtrans.repository.EmailVerificationRepository;
import bzh.stack.apiavtrans.repository.EntretienRepository;
import bzh.stack.apiavtrans.repository.NotificationRepository;
import bzh.stack.apiavtrans.repository.PasswordResetTokenRepository;
import bzh.stack.apiavtrans.repository.RapportVehiculeRepository;
import bzh.stack.apiavtrans.repository.RoleRepository;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.SignatureRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import bzh.stack.apiavtrans.repository.VehiculeAdjustInfoRepository;
import bzh.stack.apiavtrans.repository.VehiculeKilometrageRepository;
import bzh.stack.apiavtrans.repository.RapportVehiculePictureRepository;
import bzh.stack.apiavtrans.repository.EntretienFileRepository;
import bzh.stack.apiavtrans.repository.VehiculeAdjustInfoPictureRepository;
import bzh.stack.apiavtrans.repository.CarteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ServiceRepository serviceRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AbsenceRepository absenceRepository;
    private final AcompteRepository acompteRepository;
    private final SignatureRepository signatureRepository;
    private final RapportVehiculeRepository rapportVehiculeRepository;
    private final EntretienRepository entretienRepository;
    private final NotificationRepository notificationRepository;
    private final CouchetteRepository couchetteRepository;
    private final VehiculeAdjustInfoRepository vehiculeAdjustInfoRepository;
    private final VehiculeKilometrageRepository vehiculeKilometrageRepository;
    private final RapportVehiculePictureRepository rapportVehiculePictureRepository;
    private final EntretienFileRepository entretienFileRepository;
    private final VehiculeAdjustInfoPictureRepository vehiculeAdjustInfoPictureRepository;
    private final CarteRepository carteRepository;
    private final UserMapper userMapper;
    private final ServiceMapper serviceMapper;
    private final VehiculeKilometrageMapper vehiculeKilometrageMapper;
    private final EmailService emailService;

    /**
     * Créer un nouvel utilisateur
     */
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Modifier un utilisateur existant
     */
    @Transactional
    public User updateUser(UUID uuid, User updatedUser) {
        User existingUser = userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + uuid));

        existingUser.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail().toLowerCase() : null);
        existingUser.setPasswordHash(updatedUser.getPasswordHash());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setIsActive(updatedUser.getIsActive());
        existingUser.setIsMailVerified(updatedUser.getIsMailVerified());

        return userRepository.save(existingUser);
    }

    /**
     * Supprimer un utilisateur et toutes ses données associées
     */
    @Transactional
    public void deleteUser(UUID uuid) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + uuid));

        // Set user to null for related data to keep history
        absenceRepository.setValidatedByToNull(user);
        acompteRepository.setValidatedByToNull(user);
        vehiculeKilometrageRepository.setUserToNull(user);

        // Delete pictures/files linked to rapports, entretiens and adjust infos first
        rapportVehiculePictureRepository.deleteByRapportVehiculeUser(user);
        entretienFileRepository.deleteByEntretienMecanicien(user);
        vehiculeAdjustInfoPictureRepository.deleteByAdjustInfoUser(user);

        // Delete all related data
        serviceRepository.deleteByUser(user);
        emailVerificationRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);
        absenceRepository.deleteByUser(user);
        acompteRepository.deleteByUser(user);
        signatureRepository.deleteByUser(user);
        rapportVehiculeRepository.deleteByUser(user);
        entretienRepository.deleteByMecanicien(user);
        notificationRepository.deleteByUser(user);
        couchetteRepository.deleteByUser(user);
        vehiculeAdjustInfoRepository.deleteByUser(user);
        carteRepository.deleteByUser(user);

        userRepository.delete(user);
    }

    /**
     * Modifier le rôle d'un utilisateur
     */
    @Transactional
    public User updateUserRole(UUID userUuid, UUID roleUuid) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        Role role = null;
        if (roleUuid != null) {
            role = roleRepository.findById(roleUuid)
                    .orElseThrow(() -> new RuntimeException("Role not found with uuid: " + roleUuid));
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Récupérer un utilisateur par son UUID
     */
    public Optional<User> getUser(UUID uuid) {
        return userRepository.findById(uuid);
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    /**
     * Récupérer tous les utilisateurs (paginé)
     */
    public Page<User> getAllUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase()).orElse(null);
    }

    /**
     * Mettre à jour un utilisateur (admin)
     */
    @Transactional
    public User updateUserAdmin(UUID uuid, String firstName, String lastName, Boolean isActive, UUID roleUuid, Boolean isCouchette) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + uuid));

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (isActive != null) {
            user.setIsActive(isActive);
        }
        if (roleUuid != null) {
            Role role = roleRepository.findById(roleUuid)
                    .orElseThrow(() -> new RuntimeException("Role not found with uuid: " + roleUuid));
            user.setRole(role);
        }
        if (isCouchette != null) {
            user.setIsCouchette(isCouchette);
        }

        return userRepository.save(user);
    }

    /**
     * Récupérer tous les utilisateurs avec leur statut de présence (sans heures)
     */
    public List<UserDTO> getAllUsersWithStatusOnly() {
        List<User> users = userRepository.findAllByOrderByLastNameAscFirstNameAsc();
        List<UserDTO> result = new ArrayList<>();

        for (User user : users) {
            UserDTO dto = userMapper.toDTO(user);

            Optional<bzh.stack.apiavtrans.entity.Service> activeService =
                serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user);

            if (activeService.isPresent()) {
                bzh.stack.apiavtrans.entity.Service service = activeService.get();
                if (service.getIsBreak()) {
                    dto.setStatus("ON_BREAK");
                } else {
                    dto.setStatus("PRESENT");
                }
            } else {
                dto.setStatus("ABSENT");
            }

            result.add(dto);
        }

        return result;
    }

    /**
     * Récupérer tous les utilisateurs avec leur statut de présence et heures travaillées
     */
    public List<UserWithStatusDTO> getAllUsersWithStatus() {
        List<User> users = userRepository.findAllByOrderByLastNameAscFirstNameAsc();
        List<UserWithStatusDTO> result = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

        for (User user : users) {
            UserWithStatusDTO dto = new UserWithStatusDTO();
            var userDTO = userMapper.toDTO(user);
            dto.setUuid(user.getUuid());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setRole(userDTO.getRole());
            dto.setPictureUrl(userDTO.getPictureUrl());
            dto.setIsCouchette(user.getIsCouchette());

            Optional<bzh.stack.apiavtrans.entity.Service> activeService =
                serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user);

            if (activeService.isPresent()) {
                bzh.stack.apiavtrans.entity.Service service = activeService.get();
                dto.setActiveService(serviceMapper.toDTO(service));

                if (service.getIsBreak()) {
                    dto.setStatus("ON_BREAK");
                } else {
                    dto.setStatus("PRESENT");
                }
            } else {
                dto.setStatus("ABSENT");
                dto.setActiveService(null);
            }

            dto.setHoursToday(calculateHoursForPeriod(user, now, "day"));

            result.add(dto);
        }

        return result;
    }

    /**
     * Récupérer le statut d'un utilisateur spécifique par UUID
     */
    public UserWithStatusDTO getUserStatusByUuid(UUID userUuid) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

        UserWithStatusDTO dto = new UserWithStatusDTO();
        var userDTO = userMapper.toDTO(user);
        dto.setUuid(user.getUuid());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(userDTO.getRole());
        dto.setPictureUrl(userDTO.getPictureUrl());
        dto.setIsCouchette(user.getIsCouchette());

        Optional<bzh.stack.apiavtrans.entity.Service> activeService =
                serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user);

        if (activeService.isPresent()) {
            bzh.stack.apiavtrans.entity.Service service = activeService.get();
            dto.setActiveService(serviceMapper.toDTO(service));

            if (service.getIsBreak()) {
                dto.setStatus("ON_BREAK");
            } else {
                dto.setStatus("PRESENT");
            }
        } else {
            dto.setStatus("ABSENT");
            dto.setActiveService(null);
        }

        dto.setHoursToday(calculateHoursForPeriod(user, now, "day"));

        return dto;
    }

    @Transactional(readOnly = true)
    public UsersHoursListResponse getAllUsersWithHours() {
        List<User> allUsers = userRepository.findAllByOrderByLastNameAscFirstNameAsc();
        List<UserWithHoursDTO> usersWithHours = new ArrayList<>();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

        for (User user : allUsers) {
            UserDTO userDTO = userMapper.toDTO(user);

            Optional<bzh.stack.apiavtrans.entity.Service> activeService =
                serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user);

            if (activeService.isPresent()) {
                bzh.stack.apiavtrans.entity.Service service = activeService.get();
                if (service.getIsBreak()) {
                    userDTO.setStatus("ON_BREAK");
                } else {
                    userDTO.setStatus("PRESENT");
                }
            } else {
                userDTO.setStatus("ABSENT");
            }

            double hoursDay = calculateHoursForPeriod(user, now, "day");
            double hoursWeek = calculateHoursForPeriod(user, now, "week");
            double hoursMonth = calculateHoursForPeriod(user, now, "month");
            double hoursYear = calculateHoursForPeriod(user, now, "year");
            double hoursLastMonth = calculateHoursForPeriod(user, now, "lastmonth");

            UserHoursDTO hoursDTO = new UserHoursDTO(hoursDay, hoursWeek, hoursMonth, hoursYear, hoursLastMonth);
            usersWithHours.add(new UserWithHoursDTO(userDTO, hoursDTO));
        }

        return new UsersHoursListResponse(true,
                "Tous les utilisateurs avec leurs heures récupérés avec succès",
                usersWithHours);
    }

    private double calculateHoursForPeriod(User user, ZonedDateTime now, String period) {
        ZonedDateTime start;
        ZonedDateTime end;

        switch (period.toLowerCase()) {
            case "day":
                start = now.toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris"));
                end = start.plusDays(1).minusNanos(1);
                break;
            case "week":
                start = now.with(java.time.temporal.WeekFields.ISO.dayOfWeek(), 1)
                        .toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris"));
                end = start.plusWeeks(1).minusNanos(1);
                break;
            case "month":
                start = ZonedDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusMonths(1).minusNanos(1);
                break;
            case "year":
                start = ZonedDateTime.of(now.getYear(), 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusYears(1).minusNanos(1);
                break;
            case "lastmonth":
                ZonedDateTime firstDayOfCurrentMonth = ZonedDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                start = firstDayOfCurrentMonth.minusMonths(1);
                end = firstDayOfCurrentMonth.minusNanos(1);
                break;
            default:
                return 0.0;
        }

        List<bzh.stack.apiavtrans.entity.Service> services =
                serviceRepository.findByUserAndDebutBetween(user, start, end);

        long totalSeconds = 0;
        for (bzh.stack.apiavtrans.entity.Service service : services) {
            long duration = calculateServiceDuration(service, now);
            if (duration > 0) {
                if (service.getIsBreak()) {
                    totalSeconds -= duration;
                } else {
                    totalSeconds += duration;
                }
            }
        }

        double hours = totalSeconds / 3600.0;
        return Math.round(hours * 100.0) / 100.0;
    }

    private long calculateServiceDuration(bzh.stack.apiavtrans.entity.Service service, ZonedDateTime calculationTime) {
        if (service.getDuree() != null) {
            return service.getDuree();
        } else if (service.getFin() == null && service.getDebut() != null) {
            return java.time.Duration.between(service.getDebut(), calculationTime).getSeconds();
        }
        return 0;
    }

    @Transactional(readOnly = true)
    public UserLastKilometrageDTO getUserLastKilometrage(User user) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris"));
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        Optional<VehiculeKilometrage> lastKilometrage = vehiculeKilometrageRepository.findLatestByUser(user);
        boolean hasEnteredToday = vehiculeKilometrageRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

        return new UserLastKilometrageDTO(
                lastKilometrage.map(vehiculeKilometrageMapper::toDTO).orElse(null),
                hasEnteredToday
        );
    }

    @Transactional
    public User updateNotificationPreferences(User user, UpdateNotificationPreferencesRequest request) {
        if (request.getAcompte() != null) {
            user.setNotifPrefAcompte(request.getAcompte());
        }
        if (request.getAbsence() != null) {
            user.setNotifPrefAbsence(request.getAbsence());
        }
        if (request.getUserCreated() != null) {
            user.setNotifPrefUserCreated(request.getUserCreated());
        }
        if (request.getRapportVehicule() != null) {
            user.setNotifPrefRapportVehicule(request.getRapportVehicule());
        }
        if (request.getTodo() != null) {
            user.setNotifPrefTodo(request.getTodo());
        }
        return userRepository.save(user);
    }

    /**
     * Met à jour l'email d'un utilisateur et envoie l'email de vérification.
     * L'email est envoyé AVANT la sauvegarde en base pour s'assurer qu'il est bien envoyé.
     */
    @Transactional
    public User updateEmailAndSendVerification(UUID userUuid, String newEmail) {
        String normalizedEmail = newEmail.toLowerCase();

        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'uuid: " + userUuid));

        // Check if the email is already used by another user
        Optional<User> existingUserWithEmail = userRepository.findByEmail(normalizedEmail);
        if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getUuid().equals(userUuid)) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur: " + normalizedEmail);
        }

        // Create verification token first
        EmailVerification verification = new EmailVerification();
        verification.setEmail(normalizedEmail);
        verification.setToken(UUID.randomUUID().toString());
        verification.setVerificationDateTime(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        verification.setUser(user);

        // Send email BEFORE saving to database - if it fails, nothing is saved
        emailService.sendVerificationEmail(normalizedEmail, verification.getToken());

        // Email sent successfully, now update the user and save verification
        user.setEmail(normalizedEmail);
        user.setIsMailVerified(false);

        // Delete existing verification tokens
        emailVerificationRepository.deleteByUser(user);

        // Save verification token
        emailVerificationRepository.save(verification);

        return userRepository.save(user);
    }
}
