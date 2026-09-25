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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceModificationServiceTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final ZonedDateTime DEBUT = ZonedDateTime.of(2026, 9, 12, 8, 0, 0, 0, PARIS);
    private static final ZonedDateTime FIN = ZonedDateTime.of(2026, 9, 12, 17, 0, 0, 0, PARIS);

    @Mock
    private ServiceModificationRepository serviceModificationRepository;
    @Mock
    private ServiceModificationMapper serviceModificationMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ServiceModificationService serviceModificationService;

    private User admin;
    private User employee;
    private Service service;

    @BeforeEach
    void setUp() {
        admin = buildUser("Jean", "Dupont");
        employee = buildUser("Paul", "Martin");

        service = new Service();
        service.setUuid(UUID.randomUUID());
        service.setUser(employee);
        service.setDebut(DEBUT.plusMinutes(30));
        service.setFin(FIN);
        service.setIsBreak(false);
    }

    @Test
    void should_record_old_and_new_values_when_logging_update() {
        when(serviceModificationRepository.save(any(ServiceModification.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceModification saved = serviceModificationService.logUpdate(service, DEBUT, FIN, false, admin);

        assertThat(saved.getAction()).isEqualTo(Action.UPDATE);
        assertThat(saved.getServiceUuid()).isEqualTo(service.getUuid());
        assertThat(saved.getUser()).isEqualTo(employee);
        assertThat(saved.getModifiedBy()).isEqualTo(admin);
        assertThat(saved.getOldDebut()).isEqualTo(DEBUT);
        assertThat(saved.getOldFin()).isEqualTo(FIN);
        assertThat(saved.getOldIsBreak()).isFalse();
        assertThat(saved.getNewDebut()).isEqualTo(DEBUT.plusMinutes(30));
        assertThat(saved.getNewFin()).isEqualTo(FIN);
        assertThat(saved.getNewIsBreak()).isFalse();
    }

    @Test
    void should_notify_other_admins_excluding_author_when_logging_update() {
        when(serviceModificationRepository.save(any(ServiceModification.class))).thenAnswer(inv -> inv.getArgument(0));

        serviceModificationService.logUpdate(service, DEBUT, FIN, false, admin);

        NotificationCreateRequest notification = captureNotification();
        assertThat(notification.getTitle()).isEqualTo("Pointage modifié");
        assertThat(notification.getDescription()).isEqualTo(
                "Jean Dupont a modifié le service de Paul Martin : 12/09/2026 08:00 → 17:00 devient 12/09/2026 08:30 → 17:00");
        assertThat(notification.getRefType()).isEqualTo("service_modification");
        assertThat(notification.getRefId()).isEqualTo(service.getUuid().toString());
    }

    @Test
    void should_mention_type_change_when_isBreak_changes() {
        when(serviceModificationRepository.save(any(ServiceModification.class))).thenAnswer(inv -> inv.getArgument(0));
        service.setIsBreak(true);

        serviceModificationService.logUpdate(service, DEBUT, FIN, false, admin);

        assertThat(captureNotification().getDescription()).endsWith("(service → pause)");
    }

    @Test
    void should_record_only_new_values_when_logging_creation() {
        when(serviceModificationRepository.save(any(ServiceModification.class))).thenAnswer(inv -> inv.getArgument(0));
        service.setDebut(DEBUT);
        service.setFin(null);

        ServiceModification saved = serviceModificationService.logCreation(service, admin);

        assertThat(saved.getAction()).isEqualTo(Action.CREATE);
        assertThat(saved.getOldDebut()).isNull();
        assertThat(saved.getOldFin()).isNull();
        assertThat(saved.getOldIsBreak()).isNull();
        assertThat(saved.getNewDebut()).isEqualTo(DEBUT);
        assertThat(saved.getNewFin()).isNull();
        NotificationCreateRequest notification = captureNotification();
        assertThat(notification.getTitle()).isEqualTo("Pointage ajouté");
        assertThat(notification.getDescription()).isEqualTo(
                "Jean Dupont a ajouté un service à Paul Martin : 12/09/2026 08:00 → en cours");
    }

    @Test
    void should_record_only_old_values_when_logging_deletion() {
        when(serviceModificationRepository.save(any(ServiceModification.class))).thenAnswer(inv -> inv.getArgument(0));
        service.setIsBreak(true);
        service.setDebut(DEBUT.withHour(23));
        service.setFin(FIN.plusDays(1).withHour(1));

        ServiceModification saved = serviceModificationService.logDeletion(service, admin);

        assertThat(saved.getAction()).isEqualTo(Action.DELETE);
        assertThat(saved.getOldIsBreak()).isTrue();
        assertThat(saved.getNewDebut()).isNull();
        assertThat(saved.getNewFin()).isNull();
        assertThat(saved.getNewIsBreak()).isNull();
        NotificationCreateRequest notification = captureNotification();
        assertThat(notification.getTitle()).isEqualTo("Pointage supprimé");
        assertThat(notification.getDescription()).isEqualTo(
                "Jean Dupont a supprimé la pause de Paul Martin : 12/09/2026 23:00 → 13/09/2026 01:00");
    }

    @Test
    void should_fallback_to_email_when_user_has_no_name() {
        when(serviceModificationRepository.save(any(ServiceModification.class))).thenAnswer(inv -> inv.getArgument(0));
        employee.setFirstName(null);
        employee.setLastName(null);

        serviceModificationService.logDeletion(service, admin);

        assertThat(captureNotification().getDescription()).contains("de paul.martin@avtrans.fr :");
    }

    @Test
    void should_return_mapped_history_when_service_has_modifications() {
        ServiceModification modification = new ServiceModification();
        ServiceModificationDTO dto = new ServiceModificationDTO();
        when(serviceModificationRepository.findByServiceUuidOrderByCreatedAtDesc(service.getUuid()))
                .thenReturn(List.of(modification));
        when(serviceModificationMapper.toDTO(modification)).thenReturn(dto);

        ServiceModificationListResponse response = serviceModificationService.getModificationsForService(service.getUuid());

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).containsExactly(dto);
    }

    @Test
    void should_return_empty_history_when_service_has_no_modifications() {
        when(serviceModificationRepository.findByServiceUuidOrderByCreatedAtDesc(service.getUuid())).thenReturn(List.of());

        ServiceModificationListResponse response = serviceModificationService.getModificationsForService(service.getUuid());

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_use_default_pagination_sorted_by_date_when_search_request_is_null() {
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(serviceModificationRepository.findAll(any(Specification.class), pageable.capture()))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(1), 0));

        PagedResponse<ServiceModificationDTO> response = serviceModificationService.searchModifications(null);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getContent()).isEmpty();
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_accept_lowercase_action_when_searching() {
        ServiceModificationSearchRequest request = new ServiceModificationSearchRequest();
        request.setAction("delete");
        request.setPage(2);
        request.setSize(5);
        when(serviceModificationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(1), 0));

        PagedResponse<ServiceModificationDTO> response = serviceModificationService.searchModifications(request);

        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(5);
    }

    @Test
    void should_throw_when_action_filter_is_invalid() {
        ServiceModificationSearchRequest request = new ServiceModificationSearchRequest();
        request.setAction("RENAME");

        assertThatThrownBy(() -> serviceModificationService.searchModifications(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RENAME");
        verifyNoInteractions(serviceModificationRepository);
    }

    private NotificationCreateRequest captureNotification() {
        ArgumentCaptor<NotificationCreateRequest> captor = ArgumentCaptor.forClass(NotificationCreateRequest.class);
        verify(notificationService).sendNotificationToRoleWithPreferenceExcluding(
                eq("Administrateur"), captor.capture(), eq("service_modification"), eq(admin.getUuid()));
        return captor.getValue();
    }

    private User buildUser(String firstName, String lastName) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail((firstName + "." + lastName).toLowerCase() + "@avtrans.fr");
        return user;
    }
}
