package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.absence.AbsenceDTO;
import bzh.stack.apiavtrans.dto.absence.AbsenceDecompteDTO;
import bzh.stack.apiavtrans.dto.absence.AbsenceDecompteRequest;
import bzh.stack.apiavtrans.dto.absence.AbsenceDecompteResponse;
import bzh.stack.apiavtrans.dto.absence.AbsenceHeuresRequest;
import bzh.stack.apiavtrans.dto.absence.AbsenceResponse;
import bzh.stack.apiavtrans.dto.absence.AdminAbsenceUpdateRequest;
import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.Absence.AbsencePeriod;
import bzh.stack.apiavtrans.entity.Absence.AbsenceStatus;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.AbsenceType.ModeDecompte;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.AbsenceMapper;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.AbsenceTypeRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceTest {

    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AbsenceTypeRepository absenceTypeRepository;
    @Mock
    private AbsenceMapper absenceMapper;
    @Mock
    private NotificationService notificationService;
    @Spy
    private HeuresAbsenceCalculator heuresAbsenceCalculator = new HeuresAbsenceCalculator(new JoursFeriesService());

    @InjectMocks
    private AbsenceService absenceService;

    private User employe;
    private AbsenceType conges;
    private Absence absence;

    @BeforeEach
    void setUp() {
        employe = new User();
        employe.setUuid(UUID.randomUUID());
        employe.setEmail("chauffeur@avtrans.fr");
        employe.setHeureContrat(151.67);

        conges = new AbsenceType();
        conges.setUuid(UUID.randomUUID());
        conges.setName("Congés payés");
        conges.setModeDecompte(ModeDecompte.JOURS_OUVRABLES);
        conges.setCompteHeures(true);

        absence = new Absence();
        absence.setUuid(UUID.randomUUID());
        absence.setUser(employe);
        absence.setAbsenceType(conges);
        absence.setStartDate(LocalDate.of(2026, 10, 5));
        absence.setEndDate(LocalDate.of(2026, 10, 9));
        absence.setPeriod(AbsencePeriod.FULL_DAY);
        absence.setStatus(AbsenceStatus.PENDING);
    }

    // ── Heures forcées ──

    @Test
    void should_store_rounded_forced_hours_when_admin_sets_them() {
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(absenceMapper.toDTO(any(Absence.class))).thenReturn(new AbsenceDTO());

        AbsenceResponse response = absenceService.setHeuresForcees(absence.getUuid(), new AbsenceHeuresRequest(30.456));

        assertThat(response.isSuccess()).isTrue();
        assertThat(absence.getHeuresForcees()).isEqualTo(30.46);
    }

    @Test
    void should_reset_to_automatic_hours_when_forced_hours_are_null() {
        absence.setHeuresForcees(12.0);
        absence.setStatus(AbsenceStatus.APPROVED);
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(absenceMapper.toDTO(any(Absence.class))).thenReturn(new AbsenceDTO());

        AbsenceResponse response = absenceService.setHeuresForcees(absence.getUuid(), new AbsenceHeuresRequest(null));

        assertThat(response.isSuccess()).isTrue();
        assertThat(absence.getHeuresForcees()).isNull();
    }

    @Test
    void should_reject_forced_hours_when_negative() {
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceMapper.toDTO(any(Absence.class))).thenReturn(new AbsenceDTO());

        AbsenceResponse response = absenceService.setHeuresForcees(absence.getUuid(), new AbsenceHeuresRequest(-1.0));

        assertThat(response.isSuccess()).isFalse();
        assertThat(absence.getHeuresForcees()).isNull();
        verify(absenceRepository, never()).save(any(Absence.class));
    }

    @Test
    void should_reset_forced_hours_when_admin_changes_dates() {
        absence.setHeuresForcees(20.0);
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceRepository.findOverlappingAbsences(eq(employe), any(), any(), any())).thenReturn(new ArrayList<>());
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(absenceMapper.toDTO(any(Absence.class))).thenReturn(new AbsenceDTO());
        AdminAbsenceUpdateRequest request = new AdminAbsenceUpdateRequest();
        request.setEndDate(LocalDate.of(2026, 10, 7));

        AbsenceResponse response = absenceService.updateAbsenceByAdmin(absence.getUuid(), request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(absence.getHeuresForcees()).isNull();
    }

    @Test
    void should_keep_forced_hours_when_admin_only_changes_reason() {
        absence.setHeuresForcees(20.0);
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceRepository.findOverlappingAbsences(eq(employe), any(), any(), any())).thenReturn(new ArrayList<>());
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(absenceMapper.toDTO(any(Absence.class))).thenReturn(new AbsenceDTO());
        AdminAbsenceUpdateRequest request = new AdminAbsenceUpdateRequest();
        request.setReason("Vacances");
        request.setAbsenceTypeUuid(conges.getUuid());
        when(absenceTypeRepository.findById(conges.getUuid())).thenReturn(Optional.of(conges));

        absenceService.updateAbsenceByAdmin(absence.getUuid(), request);

        assertThat(absence.getHeuresForcees()).isEqualTo(20.0);
    }

    // ── Aperçu du décompte ──

    @Test
    void should_preview_count_with_user_contract_when_employee_asks() {
        when(userRepository.findByEmail(employe.getEmail())).thenReturn(Optional.of(employe));
        when(absenceTypeRepository.findById(conges.getUuid())).thenReturn(Optional.of(conges));
        when(absenceMapper.toDecompteDTO(any())).thenAnswer(inv -> {
            HeuresAbsenceCalculator.AbsenceDecompte decompte = inv.getArgument(0);
            AbsenceDecompteDTO dto = new AbsenceDecompteDTO();
            dto.setJoursDecomptes(decompte.joursDecomptes());
            dto.setHeures(decompte.heures());
            return dto;
        });

        AbsenceDecompteResponse response = absenceService.getDecompte(employe.getEmail(),
                new AbsenceDecompteRequest(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9), "FULL_DAY",
                        conges.getUuid(), null));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getJoursDecomptes()).isEqualTo(6.0);
        assertThat(response.getData().getHeures()).isEqualTo(35.0);
    }

    @Test
    void should_refuse_preview_when_start_is_after_end() {
        when(userRepository.findByEmail(employe.getEmail())).thenReturn(Optional.of(employe));

        AbsenceDecompteResponse response = absenceService.getDecompte(employe.getEmail(),
                new AbsenceDecompteRequest(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 5), null, null, null));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
    }

    @Test
    void should_refuse_admin_preview_when_user_is_missing() {
        AbsenceDecompteResponse response = absenceService.getDecompteForUser(
                new AbsenceDecompteRequest(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9), null, null, null));

        assertThat(response.isSuccess()).isFalse();
        verify(userRepository, never()).findById(any());
    }

    @Test
    void should_preview_count_for_given_user_when_admin_asks() {
        when(userRepository.findById(employe.getUuid())).thenReturn(Optional.of(employe));
        when(absenceMapper.toDecompteDTO(any())).thenReturn(new AbsenceDecompteDTO());

        AbsenceDecompteResponse response = absenceService.getDecompteForUser(
                new AbsenceDecompteRequest(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9), null, null,
                        employe.getUuid()));

        assertThat(response.isSuccess()).isTrue();
        verify(heuresAbsenceCalculator).decompter(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9),
                AbsencePeriod.FULL_DAY, ModeDecompte.JOURS_OUVRABLES, true, 151.67, null);
    }
}
