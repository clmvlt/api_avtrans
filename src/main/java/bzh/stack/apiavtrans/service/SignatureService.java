package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import bzh.stack.apiavtrans.dto.signature.*;
import bzh.stack.apiavtrans.entity.Signature;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.SignatureMapper;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.SignatureRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final SignatureRepository signatureRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final SignatureMapper signatureMapper;
    private final UserMapper userMapper;

    @Transactional
    public SignatureResponse createSignature(String userEmail, SignatureCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Signature signature = new Signature();
        signature.setSignatureBase64(request.getSignatureBase64());
        signature.setDate(request.getDate());
        signature.setHeuresSignees(request.getHeuresSignees());
        signature.setUser(user);

        Signature savedSignature = signatureRepository.save(signature);

        return new SignatureResponse(true, "Signature créée avec succès", signatureMapper.toDTO(savedSignature));
    }

    @Transactional(readOnly = true)
    public SignatureListResponse getUserSignatures(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        List<Signature> signatures = signatureRepository.findByUserOrderByDateDesc(user);
        List<SignatureDTO> signatureDTOs = signatures.stream()
                .map(signatureMapper::toDTO)
                .collect(Collectors.toList());

        return new SignatureListResponse(true, "Signatures récupérées avec succès", signatureDTOs);
    }

    @Transactional(readOnly = true)
    public SignatureResponse getLastSignature(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Signature lastSignature = signatureRepository.findFirstByUserOrderByDateDesc(user)
                .orElse(null);

        if (lastSignature == null) {
            return new SignatureResponse(true, "Aucune signature trouvée", null);
        }

        return new SignatureResponse(true, "Dernière signature récupérée avec succès",
                signatureMapper.toDTO(lastSignature));
    }

    @Transactional(readOnly = true)
    public LastSignatureSummaryDTO getLastSignatureSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Signature lastSignature = signatureRepository.findFirstByUserOrderByDateDesc(user)
                .orElse(null);

        LastSignatureSummaryDTO summary = new LastSignatureSummaryDTO();

        if (lastSignature != null) {
            summary.setDate(lastSignature.getDate());
            summary.setHeuresSignees(lastSignature.getHeuresSignees());
        }

        boolean needsToSign = checkIfUserNeedsToSign(user);
        summary.setNeedsToSign(needsToSign);

        double hoursLastMonth = calculateLastMonthHours(user);
        summary.setHeuresLastMonth(hoursLastMonth);

        return summary;
    }

    private double calculateLastMonthHours(User user) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        YearMonth lastMonth = YearMonth.from(now).minusMonths(1);

        ZonedDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay(ZoneId.of("Europe/Paris"));
        ZonedDateTime lastMonthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("Europe/Paris"));

        List<bzh.stack.apiavtrans.entity.Service> lastMonthServices =
                serviceRepository.findByUserAndDebutBetween(user, lastMonthStart, lastMonthEnd);

        long totalSeconds = 0;
        for (bzh.stack.apiavtrans.entity.Service service : lastMonthServices) {
            if (service.getDuree() != null) {
                if (service.getIsBreak()) {
                    totalSeconds -= service.getDuree();
                } else {
                    totalSeconds += service.getDuree();
                }
            }
        }

        double hours = totalSeconds / 3600.0;
        return Math.round(hours * 100.0) / 100.0;
    }

    private boolean checkIfUserNeedsToSign(User user) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth lastMonth = currentMonth.minusMonths(1);

        ZonedDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay(ZoneId.of("Europe/Paris"));
        ZonedDateTime lastMonthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("Europe/Paris"));

        List<bzh.stack.apiavtrans.entity.Service> lastMonthServices =
                serviceRepository.findByUserAndDebutBetween(user, lastMonthStart, lastMonthEnd);

        if (lastMonthServices.isEmpty()) {
            return false;
        }

        ZonedDateTime currentMonthStart = currentMonth.atDay(1).atStartOfDay(ZoneId.of("Europe/Paris"));
        ZonedDateTime currentMonthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("Europe/Paris"));

        List<Signature> currentMonthSignatures = signatureRepository.findByUserOrderByDateDesc(user).stream()
                .filter(sig -> !sig.getDate().isBefore(currentMonthStart) && !sig.getDate().isAfter(currentMonthEnd))
                .collect(Collectors.toList());

        return currentMonthSignatures.isEmpty();
    }

    @Transactional(readOnly = true)
    public SignatureListResponse getSignaturesByUserUuid(UUID userUuid) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with UUID: " + userUuid));

        List<Signature> signatures = signatureRepository.findByUserOrderByDateDesc(user);
        List<SignatureDTO> signatureDTOs = signatures.stream()
                .map(signatureMapper::toDTO)
                .collect(Collectors.toList());

        return new SignatureListResponse(true, "Signatures de l'utilisateur récupérées avec succès", signatureDTOs);
    }

    @Transactional(readOnly = true)
    public UsersWithLastSignatureListResponse getAllUsersWithLastSignature() {
        List<User> allUsers = userRepository.findAll();
        List<UserWithLastSignatureDTO> usersWithSignatures = new ArrayList<>();

        for (User user : allUsers) {
            UserDTO userDTO = userMapper.toDTO(user);
            Signature lastSignature = signatureRepository.findFirstByUserOrderByDateDesc(user).orElse(null);
            SignatureDTO signatureDTO = lastSignature != null ? signatureMapper.toDTO(lastSignature) : null;

            usersWithSignatures.add(new UserWithLastSignatureDTO(userDTO, signatureDTO));
        }

        return new UsersWithLastSignatureListResponse(true,
                "Tous les utilisateurs avec leurs dernières signatures récupérés avec succès",
                usersWithSignatures);
    }

    @Transactional
    public void deleteSignature(UUID signatureUuid) {
        Signature signature = signatureRepository.findById(signatureUuid)
                .orElseThrow(() -> new RuntimeException("Signature not found with UUID: " + signatureUuid));

        signatureRepository.delete(signature);
    }
}
