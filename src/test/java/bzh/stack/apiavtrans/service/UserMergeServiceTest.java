package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.UserMergeItemDTO;
import bzh.stack.apiavtrans.dto.common.UserMergePreviewDTO;
import bzh.stack.apiavtrans.entity.Role;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserMergeRepository;
import bzh.stack.apiavtrans.repository.UserMergeRepository.Donnee;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserMergeServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserMergeRepository userMergeRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserMergeService userMergeService;

    private User conserve;
    private User supprime;
    private User admin;

    @BeforeEach
    void setUp() {
        Role utilisateur = role("Utilisateur");
        conserve = user("Jean", "Dupont", utilisateur);
        supprime = user("Jean", "Dupond", utilisateur);
        admin = user("Anne", "Admin", role("Administrateur"));
        when(userRepository.findById(conserve.getUuid())).thenReturn(Optional.of(conserve));
        when(userRepository.findById(supprime.getUuid())).thenReturn(Optional.of(supprime));
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(any()))
                .thenReturn(Optional.empty());
    }

    @Test
    void should_list_only_non_empty_data_when_previewing() {
        when(userMergeRepository.compter(Donnee.POINTAGES, supprime)).thenReturn(12L);
        when(userMergeRepository.compter(Donnee.ABSENCES, supprime)).thenReturn(2L);

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getDonnees())
                .extracting(UserMergeItemDTO::getType, UserMergeItemDTO::getLibelle, UserMergeItemDTO::getNombre)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("POINTAGES", "Pointages", 12L),
                        org.assertj.core.groups.Tuple.tuple("ABSENCES", "Absences", 2L));
        assertThat(preview.getAvertissements()).isEmpty();
        assertThat(preview.getBlocages()).isEmpty();
        assertThat(preview.getFusionPossible()).isTrue();
    }

    @Test
    void should_return_empty_data_when_deleted_profile_has_nothing() {
        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getDonnees()).isEmpty();
        assertThat(preview.getFusionPossible()).isTrue();
    }

    @Test
    void should_warn_when_services_absences_couchettes_overlap() {
        when(userMergeRepository.compterServicesChevauchants(eq(supprime), eq(conserve), any())).thenReturn(3L);
        when(userMergeRepository.compterAbsencesChevauchantes(supprime, conserve)).thenReturn(1L);
        when(userMergeRepository.compterCouchettesEnDoublon(supprime, conserve)).thenReturn(2L);

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getAvertissements()).containsExactly(
                "3 pointages de Jean Dupond chevauchent ceux de Jean Dupont : ces heures seront comptées deux fois, à corriger après la fusion.",
                "1 absence de Jean Dupond chevauche celles de Jean Dupont : à vérifier après la fusion.",
                "2 couchettes de Jean Dupond tombent un jour où Jean Dupont en a déjà une : elles seront supprimées.");
        assertThat(preview.getFusionPossible()).isTrue();
    }

    @Test
    void should_warn_when_roles_differ() {
        supprime.setRole(role("Mécanicien"));

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getAvertissements())
                .containsExactly("Jean Dupond a le rôle « Mécanicien » : Jean Dupont garde le sien (« Utilisateur »).");
    }

    @Test
    void should_warn_when_role_is_null_on_one_side() {
        conserve.setRole(null);

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getAvertissements())
                .containsExactly("Jean Dupond a le rôle « Utilisateur » : Jean Dupont garde le sien (« aucun »).");
    }

    @Test
    void should_warn_when_only_deleted_profile_has_active_service() {
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(supprime))
                .thenReturn(Optional.of(new Service()));

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getAvertissements())
                .containsExactly("Le pointage en cours de Jean Dupond continuera sur le profil de Jean Dupont.");
        assertThat(preview.getFusionPossible()).isTrue();
    }

    @Test
    void should_block_when_both_profiles_have_active_service() {
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(any()))
                .thenReturn(Optional.of(new Service()));

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getBlocages())
                .containsExactly("Les deux profils ont un pointage en cours : terminez celui de Jean Dupond avant la fusion.");
        assertThat(preview.getFusionPossible()).isFalse();
    }

    @Test
    void should_block_when_requester_deletes_own_profile() {
        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), supprime);

        assertThat(preview.getBlocages()).hasSize(1);
        assertThat(preview.getBlocages().get(0)).startsWith("Vous ne pouvez pas supprimer votre propre compte");
        assertThat(preview.getFusionPossible()).isFalse();
    }

    @Test
    void should_allow_when_requester_is_kept_profile() {
        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), conserve);

        assertThat(preview.getFusionPossible()).isTrue();
    }

    @Test
    void should_add_emails_when_both_profiles_have_the_same_name() {
        supprime.setLastName("Dupont");
        supprime.setEmail("jean.perso@gmail.com");
        when(userMergeRepository.compterAbsencesChevauchantes(supprime, conserve)).thenReturn(1L);

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getAvertissements()).containsExactly(
                "1 absence de Jean Dupont (jean.perso@gmail.com) chevauche celles de Jean Dupont (jean.dupont@avtrans.fr) : à vérifier après la fusion.");
    }

    @Test
    void should_use_email_when_profile_has_no_name() {
        supprime.setFirstName(null);
        supprime.setLastName(null);
        when(userMergeRepository.compterAbsencesChevauchantes(supprime, conserve)).thenReturn(1L);

        UserMergePreviewDTO preview = userMergeService.previsualiser(conserve.getUuid(), supprime.getUuid(), admin);

        assertThat(preview.getAvertissements().get(0)).startsWith("1 absence de " + supprime.getEmail() + " chevauche");
    }

    @Test
    void should_throw_when_profiles_are_the_same() {
        assertThatThrownBy(() -> userMergeService.previsualiser(conserve.getUuid(), conserve.getUuid(), admin))
                .hasMessage("Choisissez deux profils différents");
        assertThatThrownBy(() -> userMergeService.fusionner(conserve.getUuid(), conserve.getUuid(), admin))
                .hasMessage("Choisissez deux profils différents");
        verify(userMergeRepository, never()).fusionner(any(), any());
    }

    @Test
    void should_throw_when_profile_not_found() {
        UUID inconnu = UUID.randomUUID();
        when(userRepository.findById(inconnu)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userMergeService.fusionner(conserve.getUuid(), inconnu, admin))
                .hasMessage("Profil à supprimer introuvable : " + inconnu);
        verify(userMergeRepository, never()).fusionner(any(), any());
    }

    @Test
    void should_throw_when_source_is_null() {
        assertThatThrownBy(() -> userMergeService.fusionner(conserve.getUuid(), null, admin))
                .hasMessage("Le profil à supprimer est obligatoire");
        verify(userMergeRepository, never()).fusionner(any(), any());
    }

    @Test
    void should_merge_and_return_reloaded_kept_profile() {
        User recharge = user("Jean", "Dupont", conserve.getRole());
        recharge.setUuid(conserve.getUuid());
        when(userRepository.findById(conserve.getUuid()))
                .thenReturn(Optional.of(conserve))
                .thenReturn(Optional.of(recharge));

        User resultat = userMergeService.fusionner(conserve.getUuid(), supprime.getUuid(), admin);

        verify(userMergeRepository).fusionner(supprime, conserve);
        assertThat(resultat).isSameAs(recharge);
    }

    @Test
    void should_refuse_merge_when_blocked() {
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(any()))
                .thenReturn(Optional.of(new Service()));

        assertThatThrownBy(() -> userMergeService.fusionner(conserve.getUuid(), supprime.getUuid(), admin))
                .hasMessage("Les deux profils ont un pointage en cours : terminez celui de Jean Dupond avant la fusion.");
        verify(userMergeRepository, never()).fusionner(any(), any());
    }

    private static User user(String prenom, String nom, Role role) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(prenom.toLowerCase() + "." + nom.toLowerCase() + "@avtrans.fr");
        user.setFirstName(prenom);
        user.setLastName(nom);
        user.setRole(role);
        return user;
    }

    private static Role role(String nom) {
        Role role = new Role();
        role.setUuid(UUID.randomUUID());
        role.setNom(nom);
        return role;
    }
}
