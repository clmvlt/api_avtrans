package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.acompte.AcompteAdminCreateRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteCreateRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteDTO;
import bzh.stack.apiavtrans.dto.acompte.AcompteListResponse;
import bzh.stack.apiavtrans.dto.acompte.AcompteResponse;
import bzh.stack.apiavtrans.dto.acompte.AcompteSearchRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteUpdateRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteValidationRequest;
import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.entity.Acompte;
import bzh.stack.apiavtrans.entity.Acompte.AcompteStatus;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.AcompteMapper;
import bzh.stack.apiavtrans.repository.AcompteRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import bzh.stack.apiavtrans.specification.AcompteSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AcompteService {

    private final AcompteRepository acompteRepository;
    private final UserRepository userRepository;
    private final AcompteMapper acompteMapper;
    private final NotificationService notificationService;

    public AcompteService(AcompteRepository acompteRepository,
                          UserRepository userRepository,
                          AcompteMapper acompteMapper,
                          NotificationService notificationService) {
        this.acompteRepository = acompteRepository;
        this.userRepository = userRepository;
        this.acompteMapper = acompteMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public AcompteResponse createAcompte(String userEmail, AcompteCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Acompte acompte = new Acompte();
        acompte.setUser(user);
        acompte.setMontant(request.getMontant());
        acompte.setRaison(request.getRaison());
        acompte.setStatus(AcompteStatus.PENDING);

        Acompte saved = acompteRepository.save(acompte);

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle("Nouvelle demande d'acompte");
        notificationRequest.setDescription(String.format("%s %s a demandé un acompte de %.2f€",
            user.getFirstName(), user.getLastName(), saved.getMontant()));
        notificationRequest.setRefType("acompte");
        notificationRequest.setRefId(saved.getUuid().toString());

        notificationService.sendNotificationToRoleWithPreference("Administrateur", notificationRequest, "acompte");

        return new AcompteResponse(true, "Demande d'acompte créée avec succès", acompteMapper.toDTO(saved));
    }

    @Transactional
    public AcompteResponse createAcompteAsAdmin(AcompteAdminCreateRequest request, User admin) {
        User user = userRepository.findById(request.getUserUuid())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Acompte acompte = new Acompte();
        acompte.setUser(user);
        acompte.setMontant(request.getMontant());
        acompte.setRaison(request.getRaison());

        if (Boolean.TRUE.equals(request.getApproved())) {
            acompte.setStatus(AcompteStatus.APPROVED);
            acompte.setValidatedBy(admin);
            acompte.setValidatedAt(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        } else {
            acompte.setStatus(AcompteStatus.PENDING);
        }

        Acompte saved = acompteRepository.save(acompte);

        String message = Boolean.TRUE.equals(request.getApproved())
                ? "Acompte créé et approuvé avec succès"
                : "Acompte créé avec succès";
        return new AcompteResponse(true, message, acompteMapper.toDTO(saved));
    }

    public AcompteListResponse getMyAcomptes(String userEmail, AcompteSearchRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null && endDate == null) {
            startDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now().plusYears(10);
        }

        AcompteStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = AcompteStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide: " + request.getStatus());
            }
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Acompte> spec = AcompteSpecifications.buildSearchSpec(
                user, startDate, endDate, status, request.getMontantMin(), request.getMontantMax(), request.getIsPaid()
        );

        Page<Acompte> acomptePage = acompteRepository.findAll(spec, pageable);

        List<AcompteDTO> acompteDTOs = acomptePage.getContent().stream()
                .map(acompteMapper::toDTO)
                .collect(Collectors.toList());

        return new AcompteListResponse(
                true,
                acompteDTOs,
                acomptePage.getTotalPages(),
                acomptePage.getTotalElements(),
                page
        );
    }

    public AcompteListResponse getAllAcomptes(AcompteSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null && endDate == null) {
            startDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now().plusYears(10);
        }

        AcompteStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = AcompteStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide: " + request.getStatus());
            }
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Acompte> spec = AcompteSpecifications.buildAdminSearchSpec(
                request.getUserUuid(), startDate, endDate, status, request.getMontantMin(), request.getMontantMax(), request.getIsPaid()
        );

        Page<Acompte> acomptePage = acompteRepository.findAll(spec, pageable);

        List<AcompteDTO> acompteDTOs = acomptePage.getContent().stream()
                .map(acompteMapper::toDTO)
                .collect(Collectors.toList());

        return new AcompteListResponse(
                true,
                acompteDTOs,
                acomptePage.getTotalPages(),
                acomptePage.getTotalElements(),
                page
        );
    }

    @Transactional
    public AcompteResponse validateAcompte(UUID acompteUuid, String adminEmail, AcompteValidationRequest request) {
        Acompte acompte = acompteRepository.findById(acompteUuid)
                .orElseThrow(() -> new RuntimeException("Acompte non trouvé"));

        if (acompte.getStatus() != AcompteStatus.PENDING) {
            return new AcompteResponse(false, "Cet acompte a déjà été traité", acompteMapper.toDTO(acompte));
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));

        if (request.getApproved()) {
            acompte.setStatus(AcompteStatus.APPROVED);
        } else {
            acompte.setStatus(AcompteStatus.REJECTED);
            acompte.setRejectionReason(request.getRejectionReason());
        }

        acompte.setValidatedBy(admin);
        acompte.setValidatedAt(ZonedDateTime.now(ZoneId.of("Europe/Paris")));

        Acompte saved = acompteRepository.save(acompte);

        String message = request.getApproved() ? "Acompte approuvé avec succès" : "Acompte refusé";
        return new AcompteResponse(true, message, acompteMapper.toDTO(saved));
    }

    @Transactional
    public AcompteResponse updateAcompte(UUID acompteUuid, String adminEmail, AcompteUpdateRequest request) {
        Acompte acompte = acompteRepository.findById(acompteUuid)
                .orElseThrow(() -> new RuntimeException("Acompte non trouvé"));

        if (request.getMontant() != null) {
            acompte.setMontant(request.getMontant());
        }
        if (request.getRaison() != null) {
            acompte.setRaison(request.getRaison());
        }
        if (request.getIsPaid() != null) {
            acompte.setIsPaid(request.getIsPaid());
            if (request.getIsPaid()) {
                acompte.setPaidDate(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
            } else {
                acompte.setPaidDate(null);
            }
        }

        Acompte saved = acompteRepository.save(acompte);

        return new AcompteResponse(true, "Acompte modifié avec succès", acompteMapper.toDTO(saved));
    }

    @Transactional
    public AcompteResponse deleteAcompte(UUID acompteUuid) {
        Acompte acompte = acompteRepository.findById(acompteUuid)
                .orElseThrow(() -> new RuntimeException("Acompte non trouvé"));

        acompteRepository.delete(acompte);

        return new AcompteResponse(true, "Acompte supprimé avec succès", null);
    }

    @Transactional
    public AcompteResponse cancelAcompte(UUID acompteUuid, String userEmail) {
        Acompte acompte = acompteRepository.findById(acompteUuid)
                .orElseThrow(() -> new RuntimeException("Acompte non trouvé"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!acompte.getUser().getUuid().equals(user.getUuid())) {
            return new AcompteResponse(false, "Vous ne pouvez annuler que vos propres demandes", null);
        }

        if (acompte.getStatus() != AcompteStatus.PENDING) {
            return new AcompteResponse(false, "Seules les demandes en attente peuvent être annulées", acompteMapper.toDTO(acompte));
        }

        acompteRepository.delete(acompte);

        return new AcompteResponse(true, "Demande d'acompte annulée avec succès", null);
    }

    public AcompteResponse getAcompteById(UUID acompteUuid) {
        Acompte acompte = acompteRepository.findById(acompteUuid)
                .orElseThrow(() -> new RuntimeException("Acompte non trouvé"));

        return new AcompteResponse(true, null, acompteMapper.toDTO(acompte));
    }
}
