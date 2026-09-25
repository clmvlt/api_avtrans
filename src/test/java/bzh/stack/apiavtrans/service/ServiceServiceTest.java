package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final ZonedDateTime DEBUT = ZonedDateTime.of(2026, 9, 12, 8, 0, 0, 0, PARIS);
    private static final ZonedDateTime FIN = ZonedDateTime.of(2026, 9, 12, 17, 0, 0, 0, PARIS);

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceModificationService serviceModificationService;

    @InjectMocks
    private ServiceService serviceService;

    private User admin;
    private User employee;
    private Service service;

    @BeforeEach
    void setUp() {
        admin = buildUser("admin@avtrans.fr");
        employee = buildUser("chauffeur@avtrans.fr");

        service = new Service();
        service.setUuid(UUID.randomUUID());
        service.setUser(employee);
        service.setDebut(DEBUT);
        service.setFin(FIN);
        service.setDuree(9 * 3600L);
        service.setIsBreak(false);
    }

    @Test
    void should_log_update_and_flag_service_when_admin_changes_fin() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
        ZonedDateTime newFin = FIN.plusMinutes(30);

        Service updated = serviceService.updateService(service.getUuid(), DEBUT, newFin,
                null, null, null, null, false, admin);

        assertThat(updated.getFin()).isEqualTo(newFin);
        assertThat(updated.getDuree()).isEqualTo(9 * 3600L + 30 * 60L);
        assertThat(updated.getModifiedBy()).isEqualTo(admin);
        assertThat(updated.getModifiedAt()).isNotNull();
        verify(serviceModificationService).logUpdate(updated, DEBUT, FIN, false, admin);
    }

    @Test
    void should_log_update_when_service_becomes_a_break() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Service updated = serviceService.updateService(service.getUuid(), DEBUT, FIN,
                null, null, null, null, true, admin);

        assertThat(updated.getIsBreak()).isTrue();
        verify(serviceModificationService).logUpdate(updated, DEBUT, FIN, false, admin);
    }

    @Test
    void should_log_update_when_fin_is_removed() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Service updated = serviceService.updateService(service.getUuid(), DEBUT, null,
                null, null, null, null, null, admin);

        assertThat(updated.getFin()).isNull();
        assertThat(updated.getDuree()).isNull();
        verify(serviceModificationService).logUpdate(updated, DEBUT, FIN, false, admin);
    }

    @Test
    void should_not_log_when_update_only_differs_below_the_second_or_by_timezone() {
        service.setDebut(DEBUT.plusNanos(123_456_000));
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Service updated = serviceService.updateService(service.getUuid(),
                DEBUT.plusNanos(123_000_000).withZoneSameInstant(ZoneOffset.UTC),
                FIN.withZoneSameInstant(ZoneOffset.UTC),
                48.1, -1.6, 48.2, -1.7, null, admin);

        assertThat(updated.getModifiedAt()).isNull();
        assertThat(updated.getModifiedBy()).isNull();
        verify(serviceModificationService, never()).logUpdate(any(), any(), any(), any(), any());
    }

    @Test
    void should_throw_and_not_log_when_updating_unknown_service() {
        UUID unknown = UUID.randomUUID();
        when(serviceRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.updateService(unknown, DEBUT, FIN,
                null, null, null, null, false, admin))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service not found");
        verifyNoInteractions(serviceModificationService);
    }

    @Test
    void should_log_creation_when_admin_creates_service() {
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Service created = serviceService.createServiceForUser(employee.getUuid(), DEBUT, FIN,
                null, null, null, null, null, admin);

        assertThat(created.getIsAdmin()).isTrue();
        assertThat(created.getIsBreak()).isFalse();
        assertThat(created.getModifiedBy()).isNull();
        verify(serviceModificationService).logCreation(created, admin);
    }

    @Test
    void should_log_deletion_before_deleting_service() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));

        serviceService.deleteService(service.getUuid(), admin);

        InOrder order = inOrder(serviceModificationService, serviceRepository);
        order.verify(serviceModificationService).logDeletion(service, admin);
        order.verify(serviceRepository).delete(service);
    }

    @Test
    void should_throw_and_not_log_when_deleting_unknown_service() {
        UUID unknown = UUID.randomUUID();
        when(serviceRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.deleteService(unknown, admin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(unknown.toString());
        verifyNoInteractions(serviceModificationService);
        verify(serviceRepository, never()).delete(any(Service.class));
        verify(serviceRepository, never()).deleteById(eq(unknown));
    }

    private User buildUser(String email) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        return user;
    }
}
