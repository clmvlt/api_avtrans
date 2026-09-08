package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.UserContractComparisonDTO;
import bzh.stack.apiavtrans.dto.common.UserDTO;
import bzh.stack.apiavtrans.dto.common.UserWithStatusDTO;
import bzh.stack.apiavtrans.dto.common.UsersHoursListResponse;
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
import org.mockito.junit.jupiter.MockitoExtension;

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
