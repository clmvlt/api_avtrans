package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.PagedResponse;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Journal des actions des administrateurs sur les pointages, consultable par tous les administrateurs.
 * Les autres administrateurs ne sont plus notifiés de ces actions (demande du propriétaire, 28/09/2026) :
 * le journal reste la seule trace.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceModificationService {

    private final ServiceModificationRepository serviceModificationRepository;
    private final ServiceModificationMapper serviceModificationMapper;

    @Transactional
    public ServiceModification logCreation(Service service, User admin) {
        ServiceModification modification = newModification(Action.CREATE, service, admin);
        modification.setNewDebut(service.getDebut());
        modification.setNewFin(service.getFin());
        modification.setNewIsBreak(service.getIsBreak());
        return serviceModificationRepository.save(modification);
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
        return serviceModificationRepository.save(modification);
    }

    @Transactional
    public ServiceModification logDeletion(Service service, User admin) {
        ServiceModification modification = newModification(Action.DELETE, service, admin);
        modification.setOldDebut(service.getDebut());
        modification.setOldFin(service.getFin());
        modification.setOldIsBreak(service.getIsBreak());
        return serviceModificationRepository.save(modification);
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
}
