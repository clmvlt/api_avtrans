package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.absence.*;
import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.Absence.AbsencePeriod;
import bzh.stack.apiavtrans.entity.Absence.AbsenceStatus;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.AbsenceMapper;
import bzh.stack.apiavtrans.mapper.RoleMapper;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.AbsenceSpecifications;
import bzh.stack.apiavtrans.repository.AbsenceTypeRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final AbsenceTypeRepository absenceTypeRepository;
    private final AbsenceMapper absenceMapper;
    private final RoleMapper roleMapper;
    private final NotificationService notificationService;

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseApiUrl;

    public AbsenceService(AbsenceRepository absenceRepository,
                          UserRepository userRepository,
                          AbsenceTypeRepository absenceTypeRepository,
                          AbsenceMapper absenceMapper,
                          RoleMapper roleMapper,
                          NotificationService notificationService) {
        this.absenceRepository = absenceRepository;
        this.userRepository = userRepository;
        this.absenceTypeRepository = absenceTypeRepository;
        this.absenceMapper = absenceMapper;
        this.roleMapper = roleMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public AbsenceResponse createAbsence(String userEmail, AbsenceCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return new AbsenceResponse(false, "La date de début doit être avant la date de fin", null);
        }

        AbsencePeriod period = parsePeriod(request.getPeriod());

        List<Absence> overlapping = absenceRepository.findOverlappingAbsences(
                user, request.getStartDate(), request.getEndDate(), period.name());

        if (!overlapping.isEmpty()) {
            return new AbsenceResponse(false, "Une absence existe déjà sur cette période", null);
        }

        Absence absence = new Absence();
        absence.setUser(user);
        absence.setStartDate(request.getStartDate());
        absence.setEndDate(request.getEndDate());
        absence.setReason(request.getReason());
        absence.setPeriod(period);
        absence.setStatus(AbsenceStatus.PENDING);

        if (request.getAbsenceTypeUuid() != null) {
            AbsenceType absenceType = absenceTypeRepository.findById(request.getAbsenceTypeUuid())
                    .orElseThrow(() -> new RuntimeException("Type d'absence non trouvé"));
            absence.setAbsenceType(absenceType);
        }
        absence.setCustomType(request.getCustomType());

        Absence saved = absenceRepository.save(absence);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String startDateStr = saved.getStartDate().format(dateFormatter);
        String endDateStr = saved.getEndDate().format(dateFormatter);
        String periodLabel = getPeriodLabel(period);

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle("Nouvelle demande d'absence");
        notificationRequest.setDescription(String.format("%s %s a demandé une absence du %s au %s%s",
            user.getFirstName(), user.getLastName(), startDateStr, endDateStr, periodLabel));
        notificationRequest.setRefType("absence");
        notificationRequest.setRefId(saved.getUuid().toString());

        notificationService.sendNotificationToRoleWithPreference("Administrateur", notificationRequest, "absence");

        return new AbsenceResponse(true, "Demande d'absence créée avec succès", absenceMapper.toDTO(saved));
    }

    public AbsenceListResponse getMyAbsences(String userEmail, AbsenceSearchRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "startDate";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null && endDate == null) {
            startDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now().plusYears(10);
        }

        AbsenceStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            status = AbsenceStatus.valueOf(request.getStatus().toUpperCase());
        }

        org.springframework.data.domain.Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                : org.springframework.data.domain.Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Absence> absencePage = absenceRepository.findAll(
                AbsenceSpecifications.buildSearchSpec(user, startDate, endDate, status, request.getAbsenceTypeUuid()),
                pageable);

        List<AbsenceDTO> absenceDTOs = absencePage.getContent().stream()
                .map(absenceMapper::toDTO)
                .collect(Collectors.toList());

        return new AbsenceListResponse(
                true,
                absenceDTOs,
                absencePage.getTotalPages(),
                absencePage.getTotalElements(),
                page
        );
    }


    public AbsenceListResponse getAllAbsences(AbsenceSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "startDate";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        Boolean includePast = request.getIncludePast() != null ? request.getIncludePast() : false;

        if (startDate == null && endDate == null) {
            if (includePast) {
                startDate = LocalDate.now().minusYears(10);
                endDate = LocalDate.now().minusDays(1);
            } else {
                startDate = LocalDate.now().minusDays(30);
                endDate = LocalDate.now().plusYears(10);
            }
        }

        AbsenceStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            status = AbsenceStatus.valueOf(request.getStatus().toUpperCase());
        }

        org.springframework.data.domain.Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                : org.springframework.data.domain.Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Absence> absencePage = absenceRepository.findAll(
                AbsenceSpecifications.buildSearchSpec(null, startDate, endDate, status, request.getAbsenceTypeUuid(), request.getUserUuid()),
                pageable);

        List<AbsenceDTO> absenceDTOs = absencePage.getContent().stream()
                .map(absenceMapper::toDTO)
                .collect(Collectors.toList());

        return new AbsenceListResponse(
                true,
                absenceDTOs,
                absencePage.getTotalPages(),
                absencePage.getTotalElements(),
                page
        );
    }

    @Transactional
    public AbsenceResponse deleteAbsenceByAdmin(UUID absenceUuid) {
        Absence absence = absenceRepository.findById(absenceUuid)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée"));

        absenceRepository.delete(absence);

        return new AbsenceResponse(true, "Absence supprimée avec succès", null);
    }

    @Transactional
    public AbsenceResponse validateAbsence(UUID absenceUuid, String adminEmail, AbsenceValidationRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));

        Absence absence = absenceRepository.findById(absenceUuid)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée"));

        if (absence.getStatus() != AbsenceStatus.PENDING) {
            return new AbsenceResponse(false, "Cette absence a déjà été traitée", absenceMapper.toDTO(absence));
        }

        if (request.getApproved()) {
            absence.setStatus(AbsenceStatus.APPROVED);
        } else {
            absence.setStatus(AbsenceStatus.REJECTED);
            absence.setRejectionReason(request.getRejectionReason());
        }

        absence.setValidatedBy(admin);
        absence.setValidatedAt(ZonedDateTime.now());

        Absence saved = absenceRepository.save(absence);

        String message = request.getApproved() ? "Absence approuvée" : "Absence refusée";
        return new AbsenceResponse(true, message, absenceMapper.toDTO(saved));
    }

    public AbsenceResponse getAbsenceById(UUID absenceUuid) {
        Absence absence = absenceRepository.findById(absenceUuid)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée"));

        return new AbsenceResponse(true, null, absenceMapper.toDTO(absence));
    }

    @Transactional
    public AbsenceResponse cancelAbsence(UUID absenceUuid, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Absence absence = absenceRepository.findById(absenceUuid)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée"));

        if (!absence.getUser().getUuid().equals(user.getUuid())) {
            return new AbsenceResponse(false, "Vous ne pouvez pas annuler cette absence", null);
        }

        if (absence.getStatus() != AbsenceStatus.PENDING) {
            return new AbsenceResponse(false, "Seules les absences en attente peuvent être annulées", null);
        }

        absenceRepository.delete(absence);

        return new AbsenceResponse(true, "Absence annulée avec succès", null);
    }

    public PlanningResponse getPlanning(String periodType, Integer year, Integer month, Integer week) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        if ("week".equalsIgnoreCase(periodType)) {
            int targetYear = year != null ? year : now.getYear();
            int targetWeek = week != null ? week : now.get(java.time.temporal.WeekFields.ISO.weekOfYear());

            LocalDate firstDayOfYear = LocalDate.of(targetYear, 1, 1);
            startDate = firstDayOfYear
                    .with(java.time.temporal.WeekFields.ISO.weekOfYear(), targetWeek)
                    .with(DayOfWeek.MONDAY);
            endDate = startDate.plusDays(6);
        } else {
            int targetYear = year != null ? year : now.getYear();
            int targetMonth = month != null ? month : now.getMonthValue();

            startDate = LocalDate.of(targetYear, targetMonth, 1);
            endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        }

        List<User> allUsers = userRepository.findAll();
        List<PlanningUserDTO> planningUsers = new ArrayList<>();

        for (User user : allUsers) {
            List<Absence> userAbsences = absenceRepository.findOverlappingAbsences(user, startDate, endDate);

            List<AbsenceDTO> absenceDTOs = userAbsences.stream()
                    .filter(a -> a.getStatus() == AbsenceStatus.APPROVED || a.getStatus() == AbsenceStatus.PENDING)
                    .map(absenceMapper::toDTO)
                    .collect(Collectors.toList());

            PlanningUserDTO planningUser = new PlanningUserDTO();
            planningUser.setUuid(user.getUuid());
            planningUser.setEmail(user.getEmail());
            planningUser.setFirstName(user.getFirstName());
            planningUser.setLastName(user.getLastName());
            planningUser.setRole(roleMapper.toDTO(user.getRole()));
            if (user.getPicturePath() != null) {
                planningUser.setPictureUrl(baseApiUrl + "/uploads/pictures/" + user.getPicturePath());
            }
            planningUser.setIsCouchette(user.getIsCouchette());
            planningUser.setAbsences(absenceDTOs);

            planningUsers.add(planningUser);
        }

        return new PlanningResponse(true, startDate, endDate, periodType, planningUsers);
    }

    @Transactional
    public AbsenceResponse createAbsenceByAdmin(AdminAbsenceCreateRequest request, String adminEmail) {
        User user = userRepository.findById(request.getUserUuid())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return new AbsenceResponse(false, "La date de début doit être avant la date de fin", null);
        }

        AbsencePeriod period = parsePeriod(request.getPeriod());

        List<Absence> overlapping = absenceRepository.findOverlappingAbsences(
                user, request.getStartDate(), request.getEndDate(), period.name());

        if (!overlapping.isEmpty()) {
            return new AbsenceResponse(false, "Une absence existe déjà sur cette période", null);
        }

        Absence absence = new Absence();
        absence.setUser(user);
        absence.setStartDate(request.getStartDate());
        absence.setEndDate(request.getEndDate());
        absence.setReason(request.getReason());
        absence.setPeriod(period);

        if (request.getAbsenceTypeUuid() != null) {
            AbsenceType absenceType = absenceTypeRepository.findById(request.getAbsenceTypeUuid())
                    .orElseThrow(() -> new RuntimeException("Type d'absence non trouvé"));
            absence.setAbsenceType(absenceType);
        }
        absence.setCustomType(request.getCustomType());

        if (request.getApproved() != null && request.getApproved()) {
            absence.setStatus(AbsenceStatus.APPROVED);
            absence.setValidatedBy(admin);
            absence.setValidatedAt(ZonedDateTime.now());
        } else {
            absence.setStatus(AbsenceStatus.PENDING);
        }

        Absence saved = absenceRepository.save(absence);

        return new AbsenceResponse(true, "Absence créée avec succès", absenceMapper.toDTO(saved));
    }

    private AbsencePeriod parsePeriod(String period) {
        if (period == null || period.isEmpty()) {
            return AbsencePeriod.FULL_DAY;
        }
        try {
            return AbsencePeriod.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AbsencePeriod.FULL_DAY;
        }
    }

    private String getPeriodLabel(AbsencePeriod period) {
        return switch (period) {
            case MORNING -> " (matin)";
            case AFTERNOON -> " (après-midi)";
            default -> "";
        };
    }
}
