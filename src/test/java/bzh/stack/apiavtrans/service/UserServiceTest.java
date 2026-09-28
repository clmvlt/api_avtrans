package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.UserContractComparisonDTO;
import bzh.stack.apiavtrans.dto.common.UserDTO;
import bzh.stack.apiavtrans.dto.common.UserWithStatusDTO;
import bzh.stack.apiavtrans.dto.common.UsersHoursListResponse;
import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ServiceMapper serviceMapper;
    @Spy
    private HeuresAbsenceCalculator heuresAbsenceCalculator = new HeuresAbsenceCalculator(new JoursFeriesService());

    @InjectMocks
    private UserService userService;

    private User visibleUser;
    private User hiddenUser;

    @BeforeEach
    void setUp() {
        visibleUser = buildUser("visible@avtrans.fr", true);
        hiddenUser = buildUser("hidden@avtrans.fr", false);
    }

    @Test
    void should_default_isVisible_to_true_when_user_is_created() {
        assertThat(new User().getIsVisible()).isTrue();
    }

    @Test
    void should_update_isVisible_when_provided() {
        when(userRepository.findById(visibleUser.getUuid())).thenReturn(Optional.of(visibleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserAdmin(visibleUser.getUuid(),
                null, null, null, null, null, false, null, null, null, null, null);

        assertThat(updated.getIsVisible()).isFalse();
        verify(userRepository).save(visibleUser);
    }

    @Test
    void should_keep_isVisible_when_null() {
        when(userRepository.findById(hiddenUser.getUuid())).thenReturn(Optional.of(hiddenUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserAdmin(hiddenUser.getUuid(),
                "Jean", null, null, null, null, null, null, null, null, null, null);

        assertThat(updated.getIsVisible()).isFalse();
        assertThat(updated.getFirstName()).isEqualTo("Jean");
    }

    @Test
    void should_include_hidden_users_when_listing_all_users() {
        when(userRepository.findAllByOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(visibleUser, hiddenUser));
        when(userMapper.toDTO(any(User.class))).thenAnswer(inv -> dtoOf(inv.getArgument(0)));

        List<UserDTO> result = userService.getAllUsersWithStatusOnly();

        assertThat(result).extracting(UserDTO::getUuid)
                .containsExactly(visibleUser.getUuid(), hiddenUser.getUuid());
        assertThat(result).extracting(UserDTO::getIsVisible).containsExactly(true, false);
        verify(userRepository, never()).findAllByIsVisibleTrueOrderByLastNameAscFirstNameAsc();
    }

    @Test
    void should_only_query_visible_users_when_getting_status() {
        when(userRepository.findAllByIsVisibleTrueOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(visibleUser));
        when(userMapper.toDTO(any(User.class))).thenAnswer(inv -> dtoOf(inv.getArgument(0)));

        List<UserWithStatusDTO> result = userService.getAllUsersWithStatus();

        assertThat(result).extracting(UserWithStatusDTO::getUuid).containsExactly(visibleUser.getUuid());
        verify(userRepository, never()).findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Test
    void should_only_query_visible_users_when_getting_hours() {
        when(userRepository.findAllByIsVisibleTrueOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(visibleUser));
        when(userMapper.toDTO(any(User.class))).thenAnswer(inv -> dtoOf(inv.getArgument(0)));

        UsersHoursListResponse response = userService.getAllUsersWithHours();

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getUsers()).hasSize(1);
        assertThat(response.getUsers().get(0).getUser().getUuid()).isEqualTo(visibleUser.getUuid());
        verify(userRepository, never()).findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Test
    void should_only_query_visible_users_when_getting_contract_comparison() {
        when(userRepository.findAllByIsVisibleTrueOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(visibleUser));
        when(userMapper.toDTO(any(User.class))).thenAnswer(inv -> dtoOf(inv.getArgument(0)));

        List<UserContractComparisonDTO> result = userService.getAllUsersContractComparison(2026, 9);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUuid()).isEqualTo(visibleUser.getUuid());
        verify(userRepository, never()).findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Test
    void should_credit_absences_and_holidays_when_building_contract_comparison() {
        // Novembre 2026, contrat 151,67 h : congés lun. 9 → ven. 13 (5 jours, le 11 est férié) + férié du 11
        visibleUser.setHeureContrat(151.67);
        AbsenceType conges = new AbsenceType();
        conges.setName("Congés payés");
        Absence absence = new Absence();
        absence.setUser(visibleUser);
        absence.setAbsenceType(conges);
        absence.setStartDate(LocalDate.of(2026, 11, 9));
        absence.setEndDate(LocalDate.of(2026, 11, 13));
        absence.setStatus(Absence.AbsenceStatus.APPROVED);

        when(userRepository.findById(visibleUser.getUuid())).thenReturn(Optional.of(visibleUser));
        when(serviceRepository.findByUserAndDebutBetween(any(User.class), any(), any())).thenReturn(List.of());
        when(absenceRepository.findApprovedByUserAndDateRange(visibleUser,
                LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30))).thenReturn(List.of(absence));
        when(userMapper.toDTO(any(User.class))).thenAnswer(inv -> dtoOf(inv.getArgument(0)));

        UserContractComparisonDTO result = userService.getUserContractComparison(visibleUser.getUuid(), 2026, 11);

        assertThat(result.getHeuresEffectuees()).isZero();
        assertThat(result.getHeuresAbsences()).isEqualTo(29.17);
        assertThat(result.getHeuresFeries()).isEqualTo(5.83);
        assertThat(result.getJoursFeries()).isEqualTo(1);
        assertThat(result.getHeuresTotal()).isEqualTo(35.0);
        assertThat(result.getDifferenceTotal()).isEqualTo(-116.67);
        // Champs historiques inchangés (sans les heures créditées)
        assertThat(result.getDifference()).isEqualTo(-151.67);
        assertThat(result.getJoursAbsence()).isEqualTo(5.0);
        // Prévision : total actuel + heures restantes au rythme du contrat (7 h par jour ouvré)
        assertThat(result.getHeuresParJourContrat()).isEqualTo(7.0);
        assertThat(result.getHeuresPrevisionnelles()).isEqualTo(
                HeuresAbsenceCalculator.round2(result.getHeuresTotal() + result.getHeuresRestantesPrevues()));
        assertThat(result.getDifferencePrevisionnelle()).isEqualTo(
                HeuresAbsenceCalculator.round2(result.getHeuresPrevisionnelles() - 151.67));
    }

    @Test
    void should_have_no_forecast_hours_when_contract_is_missing() {
        when(userRepository.findById(visibleUser.getUuid())).thenReturn(Optional.of(visibleUser));
        when(serviceRepository.findByUserAndDebutBetween(any(User.class), any(), any())).thenReturn(List.of());
        when(userMapper.toDTO(any(User.class))).thenAnswer(inv -> dtoOf(inv.getArgument(0)));

        UserContractComparisonDTO result = userService.getUserContractComparison(visibleUser.getUuid(), 2026, 11);

        assertThat(result.getJoursOuvresRestants()).isNotNull();
        assertThat(result.getHeuresParJourContrat()).isNull();
        assertThat(result.getHeuresRestantesPrevues()).isNull();
        assertThat(result.getHeuresPrevisionnelles()).isNull();
        assertThat(result.getDifferencePrevisionnelle()).isNull();
    }

    @Test
    void should_return_empty_list_when_no_visible_users() {
        when(userRepository.findAllByIsVisibleTrueOrderByLastNameAscFirstNameAsc()).thenReturn(List.of());

        assertThat(userService.getAllUsersWithStatus()).isEmpty();
    }

    private static User buildUser(String email, boolean visible) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setIsVisible(visible);
        return user;
    }

    private static UserDTO dtoOf(User user) {
        UserDTO dto = new UserDTO();
        dto.setUuid(user.getUuid());
        dto.setEmail(user.getEmail());
        dto.setIsVisible(user.getIsVisible());
        return dto;
    }
}
