package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.PagedResponse;
import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.dto.service.ServiceModificationDTO;
import bzh.stack.apiavtrans.dto.service.ServiceModificationListResponse;
import bzh.stack.apiavtrans.dto.service.ServiceModificationSearchRequest;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.ServiceModification;
import bzh.stack.apiavtrans.entity.ServiceModification.Action;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceModificationMapper;
import bzh.stack.apiavtrans.repository.ServiceModificationRepository;
import bzh.stack.apiavtrans.specification.ServiceModificationSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Journal des actions des administrateurs sur les pointages, consultable par tous les administrateurs.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceModificationService {

    static final String NOTIFICATION_TYPE = "service_modification";

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ServiceModificationRepository serviceModificationRepository;
    private final ServiceModificationMapper serviceModificationMapper;
    private final NotificationService notificationService;

    @Transactional
    public ServiceModification logCreation(Service service, User admin) {
        ServiceModification modification = newModification(Action.CREATE, service, admin);
        modification.setNewDebut(service.getDebut());
        modification.setNewFin(service.getFin());
        modification.setNewIsBreak(service.getIsBreak());
        return saveAndNotify(modification);
    }

    @Transactional
    public ServiceModification logUpdate(Service service, ZonedDateTime oldDebut, ZonedDateTime oldFin,
                                         Boolean oldIsBreak, User admin) {
        ServiceModification modification = newModification(Action.UPDATE, service, admin);
        modification.setOldDebut(oldDebut);
        modification.setOldFin(oldFin);
        modification.setOldIsBreak(oldIsBreak);
        modification.setNewDebut(service.getDebut());
        modification.setNewFin(service.getFin());
        modification.setNewIsBreak(service.getIsBreak());
        return saveAndNotify(modification);
    }

    @Transactional
    public ServiceModification logDeletion(Service service, User admin) {
        ServiceModification modification = newModification(Action.DELETE, service, admin);
        modification.setOldDebut(service.getDebut());
        modification.setOldFin(service.getFin());
        modification.setOldIsBreak(service.getIsBreak());
        return saveAndNotify(modification);
    }

    @Transactional(readOnly = true)
    public ServiceModificationListResponse getModificationsForService(UUID serviceUuid) {
        List<ServiceModificationDTO> modifications = serviceModificationRepository
                .findByServiceUuidOrderByCreatedAtDesc(serviceUuid).stream()
                .map(serviceModificationMapper::toDTO)
                .toList();
        return new ServiceModificationListResponse(true, modifications);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceModificationDTO> searchModifications(ServiceModificationSearchRequest request) {
        ServiceModificationSearchRequest filters = request != null ? request : new ServiceModificationSearchRequest();

        int page = filters.getPage() != null ? filters.getPage() : 0;
        int size = filters.getSize() != null ? filters.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ServiceModification> result = serviceModificationRepository.findAll(
                ServiceModificationSpecifications.buildSearchSpec(
                        filters.getUserUuid(),
                        filters.getModifiedByUuid(),
                        parseAction(filters.getAction()),
                        filters.getStartDate(),
                        filters.getEndDate()
                ),
                pageable
        );

        return new PagedResponse<>(
                true,
                result.getContent().stream().map(serviceModificationMapper::toDTO).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    private ServiceModification newModification(Action action, Service service, User admin) {
        ServiceModification modification = new ServiceModification();
        modification.setServiceUuid(service.getUuid());
        modification.setAction(action);
        modification.setUser(service.getUser());
        modification.setModifiedBy(admin);
        return modification;
    }

    private ServiceModification saveAndNotify(ServiceModification modification) {
        ServiceModification saved = serviceModificationRepository.save(modification);
        notifyOtherAdmins(saved);
        return saved;
    }

    private void notifyOtherAdmins(ServiceModification modification) {
        User admin = modification.getModifiedBy();
        String adminName = fullName(admin);
        String userName = fullName(modification.getUser());

        String title;
        String description;
        switch (modification.getAction()) {
            case CREATE -> {
                title = "Pointage ajouté";
                description = String.format("%s a ajouté %s à %s : %s",
                        adminName, kind(modification.getNewIsBreak(), false), userName,
                        formatPeriod(modification.getNewDebut(), modification.getNewFin()));
            }
            case UPDATE -> {
                title = "Pointage modifié";
                description = String.format("%s a modifié %s de %s : %s devient %s",
                        adminName, kind(modification.getOldIsBreak(), true), userName,
                        formatPeriod(modification.getOldDebut(), modification.getOldFin()),
                        formatPeriod(modification.getNewDebut(), modification.getNewFin()));
                if (!Objects.equals(modification.getOldIsBreak(), modification.getNewIsBreak())) {
                    description += String.format(" (%s → %s)",
                            shortKind(modification.getOldIsBreak()), shortKind(modification.getNewIsBreak()));
                }
            }
            default -> {
                title = "Pointage supprimé";
                description = String.format("%s a supprimé %s de %s : %s",
                        adminName, kind(modification.getOldIsBreak(), true), userName,
                        formatPeriod(modification.getOldDebut(), modification.getOldFin()));
            }
        }

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle(title);
        notificationRequest.setDescription(description);
        notificationRequest.setRefType(NOTIFICATION_TYPE);
        notificationRequest.setRefId(modification.getServiceUuid() != null ? modification.getServiceUuid().toString() : null);

        notificationService.sendNotificationToRoleWithPreferenceExcluding(
                "Administrateur", notificationRequest, NOTIFICATION_TYPE, admin != null ? admin.getUuid() : null);
    }

    private Action parseAction(String action) {
        if (action == null || action.isBlank()) {
            return null;
        }
        try {
            return Action.valueOf(action.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Action invalide : " + action + ". Valeurs possibles : CREATE, UPDATE, DELETE");
        }
    }

    private String formatPeriod(ZonedDateTime debut, ZonedDateTime fin) {
        if (debut == null) {
            return "?";
        }
        ZonedDateTime start = debut.withZoneSameInstant(PARIS);
        if (fin == null) {
            return start.format(DATE_TIME_FORMAT) + " → en cours";
        }
        ZonedDateTime end = fin.withZoneSameInstant(PARIS);
        String endLabel = end.toLocalDate().equals(start.toLocalDate())
                ? end.format(TIME_FORMAT)
                : end.format(DATE_TIME_FORMAT);
        return start.format(DATE_TIME_FORMAT) + " → " + endLabel;
    }

    private String kind(Boolean isBreak, boolean definite) {
        if (Boolean.TRUE.equals(isBreak)) {
            return definite ? "la pause" : "une pause";
        }
        return definite ? "le service" : "un service";
    }

    private String shortKind(Boolean isBreak) {
        return Boolean.TRUE.equals(isBreak) ? "pause" : "service";
    }

    private String fullName(User user) {
        if (user == null) {
            return "Un administrateur";
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String name = (firstName + " " + lastName).trim();
        return name.isEmpty() ? user.getEmail() : name;
    }
}
