package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
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

    private ServiceService serviceService;

    private User admin;
    private User employee;
    private Service service;

    @BeforeEach
    void setUp() {
        TempsTravailCalculator calculator = new TempsTravailCalculator();
        ServiceMapper mapper = new ServiceMapper(calculator);
        TempsTravailService tempsTravailService = new TempsTravailService(serviceRepository, calculator, mapper);
        serviceService = new ServiceService(serviceRepository, userRepository, serviceModificationService,
                tempsTravailService, mapper);

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

    // ── Pointage du salarié ──

    @Test
    void should_attach_break_to_running_service_when_starting_break() {
        Service enCours = runningService();
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.of(enCours));
        when(serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(enCours)).thenReturn(Optional.empty());
        ArgumentCaptor<Service> saved = ArgumentCaptor.forClass(Service.class);
        when(serviceRepository.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(serviceRepository.findByServiceParentOrderByDebutAsc(enCours))
                .thenAnswer(inv -> List.of(saved.getValue()));

        ServiceDTO etat = serviceService.startBreak(employee.getEmail(), 48.1, -1.6);

        Service pause = saved.getValue();
        assertThat(pause.getIsBreak()).isTrue();
        assertThat(pause.getServiceParent()).isSameAs(enCours);
        assertThat(etat.getUuid()).isEqualTo(enCours.getUuid());
        assertThat(etat.getEnPause()).isTrue();
        assertThat(etat.getPauses()).hasSize(1);
    }

    @Test
    void should_refuse_break_when_a_break_is_already_running() {
        Service enCours = runningService();
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.of(enCours));
        when(serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(enCours))
                .thenReturn(Optional.of(new Service()));

        assertThatThrownBy(() -> serviceService.startBreak(employee.getEmail(), null, null))
                .hasMessageContaining("already on break");
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void should_refuse_ending_service_when_user_is_on_break() {
        Service enCours = runningService();
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.of(enCours));
        when(serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(enCours))
                .thenReturn(Optional.of(new Service()));

        assertThatThrownBy(() -> serviceService.endService(employee.getEmail(), null, null))
                .hasMessageContaining("End break first");
        assertThat(enCours.getFin()).isNull();
    }

    @Test
    void should_close_running_break_and_return_work_service_when_ending_break() {
        Service enCours = runningService();
        Service pause = new Service();
        pause.setUuid(UUID.randomUUID());
        pause.setIsBreak(true);
        pause.setServiceParent(enCours);
        pause.setDebut(ZonedDateTime.now(PARIS).minusMinutes(20));
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.of(enCours));
        when(serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(enCours)).thenReturn(Optional.of(pause));
        when(serviceRepository.findByServiceParentOrderByDebutAsc(enCours)).thenReturn(List.of(pause));

        ServiceDTO etat = serviceService.endBreak(employee.getEmail(), 48.1, -1.6);

        assertThat(pause.getFin()).isNotNull();
        assertThat(pause.getDuree()).isBetween(20 * 60L - 1, 20 * 60L + 1);
        assertThat(pause.getLatitudeEnd()).isEqualTo(48.1);
        assertThat(etat.getUuid()).isEqualTo(enCours.getUuid());
        assertThat(etat.getEnPause()).isFalse();
        assertThat(etat.getEnCours()).isTrue();
    }

    @Test
    void should_refuse_starting_service_when_one_is_already_running() {
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.of(runningService()));

        assertThatThrownBy(() -> serviceService.startService(employee.getEmail(), null, null, false))
                .hasMessage("User already has an active service");
    }

    // ── Administration : modification ──

    @Test
    void should_log_update_and_flag_service_when_admin_changes_fin() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
        ZonedDateTime newFin = FIN.plusMinutes(30);

        ServiceDTO updated = serviceService.updateService(service.getUuid(), DEBUT, newFin,
                null, null, null, null, false, null, admin);

        assertThat(updated.getFin()).isEqualTo(newFin);
        assertThat(service.getDuree()).isEqualTo(9 * 3600L + 30 * 60L);
        assertThat(updated.getDureeTravail()).isEqualTo(9 * 3600L + 30 * 60L);
        assertThat(updated.getModifiedByUuid()).isEqualTo(admin.getUuid());
        assertThat(updated.getModifiedAt()).isNotNull();
        verify(serviceModificationService).logUpdate(service, DEBUT, FIN, false, admin);
    }

    @Test
    void should_attach_to_covering_service_and_log_update_when_service_becomes_a_break() {
        Service couvrant = closedService(DEBUT.minusHours(1), FIN.plusHours(1));
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.findServicesTravailContenant(employee, DEBUT)).thenReturn(List.of(couvrant));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceDTO updated = serviceService.updateService(service.getUuid(), DEBUT, FIN,
                null, null, null, null, true, null, admin);

        assertThat(updated.getIsBreak()).isTrue();
        assertThat(updated.getServiceParentUuid()).isEqualTo(couvrant.getUuid());
        verify(serviceModificationService).logUpdate(service, DEBUT, FIN, false, admin);
    }

    @Test
    void should_refuse_turning_service_into_break_when_it_has_breaks() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.findByServiceParentOrderByDebutAsc(service)).thenReturn(List.of(new Service()));

        assertThatThrownBy(() -> serviceService.updateService(service.getUuid(), DEBUT, FIN,
                null, null, null, null, true, null, admin))
                .hasMessageContaining("contient des pauses");
        verifyNoInteractions(serviceModificationService);
    }

    @Test
    void should_reattach_break_when_admin_moves_it_outside_its_service() {
        Service ancienParent = closedService(DEBUT, FIN);
        Service nouveauParent = closedService(FIN.plusHours(2), FIN.plusHours(6));
        Service pause = breakOf(ancienParent, DEBUT.plusHours(4), DEBUT.plusHours(5));
        ZonedDateTime nouveauDebut = FIN.plusHours(3);
        when(serviceRepository.findById(pause.getUuid())).thenReturn(Optional.of(pause));
        when(serviceRepository.findServicesTravailContenant(employee, nouveauDebut)).thenReturn(List.of(nouveauParent));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceDTO updated = serviceService.updateService(pause.getUuid(), nouveauDebut, nouveauDebut.plusMinutes(30),
                null, null, null, null, null, null, admin);

        assertThat(pause.getServiceParent()).isSameAs(nouveauParent);
        assertThat(updated.getServiceParentUuid()).isEqualTo(nouveauParent.getUuid());
    }

    @Test
    void should_refuse_closing_service_when_it_has_a_running_break() {
        Service enCours = runningService();
        when(serviceRepository.findById(enCours.getUuid())).thenReturn(Optional.of(enCours));
        when(serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(enCours))
                .thenReturn(Optional.of(new Service()));

        assertThatThrownBy(() -> serviceService.updateService(enCours.getUuid(), enCours.getDebut(), ZonedDateTime.now(PARIS),
                null, null, null, null, false, null, admin))
                .hasMessageContaining("pause en cours");
    }

    @Test
    void should_refuse_update_when_fin_is_before_debut() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> serviceService.updateService(service.getUuid(), FIN, DEBUT,
                null, null, null, null, false, null, admin))
                .hasMessage("La fin doit être postérieure au début");
        verifyNoInteractions(serviceModificationService);
    }

    @Test
    void should_log_update_when_fin_is_removed() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.empty());
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceDTO updated = serviceService.updateService(service.getUuid(), DEBUT, null,
                null, null, null, null, null, null, admin);

        assertThat(updated.getFin()).isNull();
        assertThat(updated.getEnCours()).isTrue();
        assertThat(service.getDuree()).isNull();
        verify(serviceModificationService).logUpdate(service, DEBUT, FIN, false, admin);
    }

    @Test
    void should_refuse_reopening_service_when_another_one_is_running() {
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(employee))
                .thenReturn(Optional.of(runningService()));

        assertThatThrownBy(() -> serviceService.updateService(service.getUuid(), DEBUT, null,
                null, null, null, null, null, null, admin))
                .hasMessageContaining("déjà un service en cours");
    }

    @Test
    void should_not_log_when_update_only_differs_below_the_second_or_by_timezone() {
        service.setDebut(DEBUT.plusNanos(123_456_000));
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceDTO updated = serviceService.updateService(service.getUuid(),
                DEBUT.plusNanos(123_000_000).withZoneSameInstant(ZoneOffset.UTC),
                FIN.withZoneSameInstant(ZoneOffset.UTC),
                48.1, -1.6, 48.2, -1.7, null, null, admin);

        assertThat(updated.getModifiedAt()).isNull();
        assertThat(service.getModifiedBy()).isNull();
        verify(serviceModificationService, never()).logUpdate(any(), any(), any(), any(), any());
    }

    @Test
    void should_throw_and_not_log_when_updating_unknown_service() {
        UUID unknown = UUID.randomUUID();
        when(serviceRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.updateService(unknown, DEBUT, FIN,
                null, null, null, null, false, null, admin))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service not found");
        verifyNoInteractions(serviceModificationService);
    }

    // ── Administration : création ──

    @Test
    void should_log_creation_when_admin_creates_service() {
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        ArgumentCaptor<Service> saved = ArgumentCaptor.forClass(Service.class);
        when(serviceRepository.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));

        ServiceDTO created = serviceService.createServiceForUser(employee.getUuid(), DEBUT, FIN,
                null, null, null, null, null, null, admin);

        assertThat(created.getIsAdmin()).isTrue();
        assertThat(created.getIsBreak()).isFalse();
        assertThat(created.getModifiedByUuid()).isNull();
        assertThat(created.getDureeTravail()).isEqualTo(9 * 3600L);
        verify(serviceModificationService).logCreation(saved.getValue(), admin);
    }

    @Test
    void should_attach_admin_break_to_service_running_at_its_start_when_no_parent_is_given() {
        Service couvrant = closedService(DEBUT, FIN);
        ZonedDateTime debutPause = DEBUT.plusHours(4);
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        when(serviceRepository.findServicesTravailContenant(employee, debutPause)).thenReturn(List.of(couvrant));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceDTO created = serviceService.createServiceForUser(employee.getUuid(), debutPause, debutPause.plusHours(1),
                null, null, null, null, true, null, admin);

        assertThat(created.getIsBreak()).isTrue();
        assertThat(created.getServiceParentUuid()).isEqualTo(couvrant.getUuid());
        assertThat(created.getDuree()).isEqualTo(3600L);
    }

    @Test
    void should_refuse_admin_break_when_no_service_covers_it() {
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        when(serviceRepository.findServicesTravailContenant(eq(employee), any())).thenReturn(List.of());

        assertThatThrownBy(() -> serviceService.createServiceForUser(employee.getUuid(), DEBUT, FIN,
                null, null, null, null, true, null, admin))
                .hasMessageContaining("Aucun service de travail");
        verify(serviceRepository, never()).save(any());
        verifyNoInteractions(serviceModificationService);
    }

    @Test
    void should_refuse_admin_break_when_parent_belongs_to_another_user() {
        Service autre = closedService(DEBUT, FIN);
        autre.setUser(admin);
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(autre.getUuid())).thenReturn(Optional.of(autre));

        assertThatThrownBy(() -> serviceService.createServiceForUser(employee.getUuid(), DEBUT.plusHours(1), DEBUT.plusHours(2),
                null, null, null, null, true, autre.getUuid(), admin))
                .hasMessageContaining("autre utilisateur");
    }

    @Test
    void should_refuse_admin_break_when_it_starts_outside_given_parent() {
        Service parent = closedService(DEBUT, FIN);
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(parent.getUuid())).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> serviceService.createServiceForUser(employee.getUuid(), FIN.plusHours(1), FIN.plusHours(2),
                null, null, null, null, true, parent.getUuid(), admin))
                .hasMessageContaining("doit commencer pendant son service");
    }

    @Test
    void should_refuse_running_admin_break_on_closed_service() {
        Service parent = closedService(DEBUT, FIN);
        when(userRepository.findById(employee.getUuid())).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(parent.getUuid())).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> serviceService.createServiceForUser(employee.getUuid(), DEBUT.plusHours(1), null,
                null, null, null, null, true, parent.getUuid(), admin))
                .hasMessageContaining("service en cours");
    }

    // ── Administration : suppression ──

    @Test
    void should_delete_breaks_before_work_service_and_log_each_deletion() {
        Service pause = breakOf(service, DEBUT.plusHours(4), DEBUT.plusHours(5));
        when(serviceRepository.findById(service.getUuid())).thenReturn(Optional.of(service));
        when(serviceRepository.findByServiceParentOrderByDebutAsc(service)).thenReturn(List.of(pause));

        serviceService.deleteService(service.getUuid(), admin);

        InOrder order = inOrder(serviceModificationService, serviceRepository);
        order.verify(serviceModificationService).logDeletion(pause, admin);
        order.verify(serviceRepository).delete(pause);
        order.verify(serviceModificationService).logDeletion(service, admin);
        order.verify(serviceRepository).delete(service);
    }

    @Test
    void should_delete_only_the_break_when_deleting_a_break() {
        Service pause = breakOf(service, DEBUT.plusHours(4), DEBUT.plusHours(5));
        when(serviceRepository.findById(pause.getUuid())).thenReturn(Optional.of(pause));

        serviceService.deleteService(pause.getUuid(), admin);

        verify(serviceRepository).delete(pause);
        verify(serviceRepository, never()).delete(service);
        verify(serviceRepository, never()).findByServiceParentOrderByDebutAsc(any());
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

    // ── Heures ──

    @Test
    void should_return_all_periods_with_live_flag_when_no_period_is_given() {
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findServicesTravail(eq(employee), any(), any())).thenReturn(List.of());

        var heures = serviceService.getWorkedHours(employee.getEmail(), null, null, null, null, null);

        assertThat(heures.getCalculeLe()).isNotNull();
        assertThat(List.of(heures.getDay(), heures.getWeek(), heures.getMonth(), heures.getYear(), heures.getLastMonth()))
                .allSatisfy(p -> {
                    assertThat(p.getSecondes()).isZero();
                    assertThat(p.getEnCours()).isFalse();
                });
    }

    @Test
    void should_return_only_requested_period_when_period_is_given() {
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findServicesTravail(eq(employee), any(), any())).thenReturn(List.of());

        var heures = serviceService.getWorkedHours(employee.getEmail(), "week", null, null, null, null);

        assertThat(heures.getWeek()).isNotNull();
        assertThat(heures.getDay()).isNull();
        assertThatThrownBy(() -> serviceService.getWorkedHours(employee.getEmail(), "decade", null, null, null, null))
                .hasMessageContaining("Invalid period");
    }

    @Test
    void should_return_yesterday_workday_when_night_service_ended_after_midnight() {
        ZonedDateTime minuit = ZonedDateTime.now(PARIS).toLocalDate().atStartOfDay(PARIS);
        Service nuit = closedService(minuit.minusHours(3), minuit.plusMinutes(1));
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findServicesTravailChevauchant(eq(employee), any(), any(), any())).thenReturn(List.of(nuit));
        when(serviceRepository.findServicesTravail(eq(employee), any(), any())).thenReturn(List.of(nuit));

        var journee = serviceService.getDailyServices(employee.getEmail());

        assertThat(journee.getDate()).isEqualTo(minuit.toLocalDate().minusDays(1));
        assertThat(journee.getAujourdhui()).isFalse();
        assertThat(journee.getServices()).extracting(ServiceDTO::getUuid).containsExactly(nuit.getUuid());
        assertThat(journee.getHeures().getSecondes()).isEqualTo(3 * 3600 + 60);
    }

    @Test
    void should_use_workday_for_day_hours_when_no_date_is_given() {
        ZonedDateTime minuit = ZonedDateTime.now(PARIS).toLocalDate().atStartOfDay(PARIS);
        Service nuit = closedService(minuit.minusHours(3), minuit.plusMinutes(1));
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(serviceRepository.findServicesTravailChevauchant(eq(employee), any(), any(), any())).thenReturn(List.of(nuit));
        when(serviceRepository.findServicesTravail(eq(employee), any(), any())).thenReturn(List.of(nuit));

        var heures = serviceService.getWorkedHours(employee.getEmail(), "day", null, null, null, null);
        var jourExplicite = serviceService.getWorkedHours(employee.getEmail(), "day", null, null, null,
                minuit.getDayOfMonth());

        assertThat(heures.getJour()).isEqualTo(minuit.toLocalDate().minusDays(1));
        assertThat(heures.getDay().getSecondes()).isEqualTo(3 * 3600 + 60);
        assertThat(jourExplicite.getJour()).isEqualTo(minuit.toLocalDate());
    }

    private Service runningService() {
        Service enCours = new Service();
        enCours.setUuid(UUID.randomUUID());
        enCours.setUser(employee);
        enCours.setDebut(ZonedDateTime.now(PARIS).minusHours(2));
        enCours.setIsBreak(false);
        return enCours;
    }

    private Service closedService(ZonedDateTime debut, ZonedDateTime fin) {
        Service s = new Service();
        s.setUuid(UUID.randomUUID());
        s.setUser(employee);
        s.setDebut(debut);
        s.setFin(fin);
        s.setIsBreak(false);
        return s;
    }

    private Service breakOf(Service parent, ZonedDateTime debut, ZonedDateTime fin) {
        Service pause = closedService(debut, fin);
        pause.setIsBreak(true);
        pause.setServiceParent(parent);
        return pause;
    }

    private User buildUser(String email) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        return user;
    }
}
