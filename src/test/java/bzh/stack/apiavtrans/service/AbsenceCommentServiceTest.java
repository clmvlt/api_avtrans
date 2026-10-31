package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.controller.AbsenceController;
import bzh.stack.apiavtrans.dto.absence.AbsenceCommentCreateRequest;
import bzh.stack.apiavtrans.dto.absence.AbsenceCommentDTO;
import bzh.stack.apiavtrans.dto.absence.AbsenceCommentListResponse;
import bzh.stack.apiavtrans.dto.absence.AbsenceCommentResponse;
import bzh.stack.apiavtrans.dto.absence.AbsenceCommentUpdateRequest;
import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceComment;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.AbsenceCommentMapper;
import bzh.stack.apiavtrans.repository.AbsenceCommentRepository;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbsenceCommentServiceTest {

    @Mock
    private AbsenceCommentRepository absenceCommentRepository;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private AbsenceCommentMapper absenceCommentMapper = new AbsenceCommentMapper();

    @InjectMocks
    private AbsenceCommentService absenceCommentService;

    private User author;
    private User otherAdmin;
    private Absence absence;
    private AbsenceComment comment;

    @BeforeEach
    void setUp() {
        author = buildAdmin("marie@avtrans.fr", "Marie", "Martin");
        otherAdmin = buildAdmin("paul@avtrans.fr", "Paul", "Durand");

        absence = new Absence();
        absence.setUuid(UUID.randomUUID());

        comment = new AbsenceComment();
        comment.setUuid(UUID.randomUUID());
        comment.setAbsence(absence);
        comment.setAuthor(author);
        comment.setContent("Texte initial");
    }

    // ── Lecture ──

    @Test
    void should_return_comments_in_repository_order_when_absence_exists() {
        AbsenceComment orphan = new AbsenceComment();
        orphan.setUuid(UUID.randomUUID());
        orphan.setAbsence(absence);
        orphan.setContent("Ancien commentaire");
        when(absenceRepository.existsById(absence.getUuid())).thenReturn(true);
        when(absenceCommentRepository.findByAbsenceUuidInOrder(absence.getUuid())).thenReturn(List.of(orphan, comment));

        AbsenceCommentListResponse response = absenceCommentService.getComments(absence.getUuid());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getComments()).extracting(AbsenceCommentDTO::getUuid)
                .containsExactly(orphan.getUuid(), comment.getUuid());
    }

    @Test
    void should_map_null_author_when_comment_author_was_deleted() {
        comment.setAuthor(null);
        when(absenceRepository.existsById(absence.getUuid())).thenReturn(true);
        when(absenceCommentRepository.findByAbsenceUuidInOrder(absence.getUuid())).thenReturn(List.of(comment));

        AbsenceCommentDTO dto = absenceCommentService.getComments(absence.getUuid()).getComments().get(0);

        assertThat(dto.getAuthorUuid()).isNull();
        assertThat(dto.getAuthorName()).isNull();
        assertThat(dto.getContent()).isEqualTo("Texte initial");
    }

    @Test
    void should_throw_when_listing_comments_of_unknown_absence() {
        UUID unknown = UUID.randomUUID();
        when(absenceRepository.existsById(unknown)).thenReturn(false);

        assertThatThrownBy(() -> absenceCommentService.getComments(unknown))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Absence non trouvée");
        verify(absenceCommentRepository, never()).findByAbsenceUuidInOrder(any());
    }

    // ── Création ──

    @Test
    void should_store_trimmed_content_when_creating_comment() {
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceCommentRepository.save(any(AbsenceComment.class))).thenAnswer(inv -> inv.getArgument(0));

        AbsenceCommentResponse response = absenceCommentService.createComment(absence.getUuid(), author.getEmail(),
                new AbsenceCommentCreateRequest("  Justificatif\nreçu \n"));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getComment().getContent()).isEqualTo("Justificatif\nreçu");
        assertThat(response.getComment().getAuthorUuid()).isEqualTo(author.getUuid());
        assertThat(response.getComment().getAuthorName()).isEqualTo("Marie Martin");
        assertThat(response.getComment().getAbsenceUuid()).isEqualTo(absence.getUuid());
    }

    @Test
    void should_reject_comment_when_content_is_blank() {
        AbsenceCommentResponse response = absenceCommentService.createComment(absence.getUuid(), author.getEmail(),
                new AbsenceCommentCreateRequest(" \n\t "));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Le commentaire ne peut pas être vide");
        verify(absenceCommentRepository, never()).save(any());
    }

    @Test
    void should_reject_comment_when_content_is_null() {
        AbsenceCommentResponse response = absenceCommentService.createComment(absence.getUuid(), author.getEmail(),
                new AbsenceCommentCreateRequest(null));

        assertThat(response.isSuccess()).isFalse();
        verify(absenceCommentRepository, never()).save(any());
    }

    @Test
    void should_reject_comment_when_content_is_too_long() {
        AbsenceCommentResponse response = absenceCommentService.createComment(absence.getUuid(), author.getEmail(),
                new AbsenceCommentCreateRequest("a".repeat(AbsenceCommentService.MAX_CONTENT_LENGTH + 1)));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("2000");
        verify(absenceCommentRepository, never()).save(any());
    }

    @Test
    void should_accept_comment_when_content_reaches_limit_once_trimmed() {
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(absenceRepository.findById(absence.getUuid())).thenReturn(Optional.of(absence));
        when(absenceCommentRepository.save(any(AbsenceComment.class))).thenAnswer(inv -> inv.getArgument(0));
        String atLimit = "a".repeat(AbsenceCommentService.MAX_CONTENT_LENGTH);

        AbsenceCommentResponse response = absenceCommentService.createComment(absence.getUuid(), author.getEmail(),
                new AbsenceCommentCreateRequest("  " + atLimit + "  "));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getComment().getContent()).isEqualTo(atLimit);
    }

    @Test
    void should_throw_when_commenting_unknown_absence() {
        UUID unknown = UUID.randomUUID();
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(absenceRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> absenceCommentService.createComment(unknown, author.getEmail(),
                new AbsenceCommentCreateRequest("Texte")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Absence non trouvée");
        verify(absenceCommentRepository, never()).save(any());
    }

    // ── Modification ──

    @Test
    void should_update_trimmed_content_when_author_edits_comment() {
        when(absenceCommentRepository.findById(comment.getUuid())).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(absenceCommentRepository.saveAndFlush(comment)).thenReturn(comment);

        AbsenceCommentResponse response = absenceCommentService.updateComment(comment.getUuid(), author.getEmail(),
                new AbsenceCommentUpdateRequest("  Nouveau texte  "));

        assertThat(response.isSuccess()).isTrue();
        assertThat(comment.getContent()).isEqualTo("Nouveau texte");
        assertThat(response.getComment().getContent()).isEqualTo("Nouveau texte");
    }

    @Test
    void should_refuse_update_when_admin_is_not_the_author() {
        when(absenceCommentRepository.findById(comment.getUuid())).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(otherAdmin.getEmail())).thenReturn(Optional.of(otherAdmin));

        AbsenceCommentResponse response = absenceCommentService.updateComment(comment.getUuid(), otherAdmin.getEmail(),
                new AbsenceCommentUpdateRequest("Détourné"));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Vous ne pouvez modifier que vos propres commentaires");
        assertThat(comment.getContent()).isEqualTo("Texte initial");
        verify(absenceCommentRepository, never()).saveAndFlush(any());
    }

    @Test
    void should_refuse_update_when_comment_author_was_deleted() {
        comment.setAuthor(null);
        when(absenceCommentRepository.findById(comment.getUuid())).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(otherAdmin.getEmail())).thenReturn(Optional.of(otherAdmin));

        AbsenceCommentResponse response = absenceCommentService.updateComment(comment.getUuid(), otherAdmin.getEmail(),
                new AbsenceCommentUpdateRequest("Détourné"));

        assertThat(response.isSuccess()).isFalse();
        verify(absenceCommentRepository, never()).saveAndFlush(any());
    }

    @Test
    void should_reject_update_when_content_is_blank() {
        AbsenceCommentResponse response = absenceCommentService.updateComment(comment.getUuid(), author.getEmail(),
                new AbsenceCommentUpdateRequest("   "));

        assertThat(response.isSuccess()).isFalse();
        assertThat(comment.getContent()).isEqualTo("Texte initial");
        verify(absenceCommentRepository, never()).saveAndFlush(any());
    }

    @Test
    void should_reject_update_when_content_is_too_long() {
        AbsenceCommentResponse response = absenceCommentService.updateComment(comment.getUuid(), author.getEmail(),
                new AbsenceCommentUpdateRequest("a".repeat(AbsenceCommentService.MAX_CONTENT_LENGTH + 1)));

        assertThat(response.isSuccess()).isFalse();
        verify(absenceCommentRepository, never()).saveAndFlush(any());
    }

    @Test
    void should_throw_when_updating_unknown_comment() {
        UUID unknown = UUID.randomUUID();
        when(absenceCommentRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> absenceCommentService.updateComment(unknown, author.getEmail(),
                new AbsenceCommentUpdateRequest("Texte")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Commentaire non trouvé");
    }

    // ── Suppression ──

    @Test
    void should_delete_comment_when_requested_by_author() {
        when(absenceCommentRepository.findById(comment.getUuid())).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));

        AbsenceCommentResponse response = absenceCommentService.deleteComment(comment.getUuid(), author.getEmail());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getComment()).isNull();
        verify(absenceCommentRepository).delete(comment);
    }

    @Test
    void should_refuse_delete_when_admin_is_not_the_author() {
        when(absenceCommentRepository.findById(comment.getUuid())).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(otherAdmin.getEmail())).thenReturn(Optional.of(otherAdmin));

        AbsenceCommentResponse response = absenceCommentService.deleteComment(comment.getUuid(), otherAdmin.getEmail());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Vous ne pouvez supprimer que vos propres commentaires");
        verify(absenceCommentRepository, never()).delete(any(AbsenceComment.class));
    }

    @Test
    void should_throw_when_deleting_unknown_comment() {
        UUID unknown = UUID.randomUUID();
        when(absenceCommentRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> absenceCommentService.deleteComment(unknown, author.getEmail()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Commentaire non trouvé");
    }

    // ── Garde-fous autour du service ──

    @Test
    void should_require_admin_role_when_exposing_comment_routes() {
        List<Method> routes = Arrays.stream(AbsenceController.class.getDeclaredMethods())
                .filter(method -> method.getName().endsWith("AbsenceComment") || method.getName().endsWith("AbsenceComments"))
                .toList();

        assertThat(routes).hasSize(4);
        assertThat(routes).allSatisfy(route ->
                assertThat(route.getAnnotation(RequireRole.class).value()).isEqualTo("Administrateur"));
    }

    @Test
    void should_keep_others_comments_and_delete_own_absence_comments_first_when_user_is_deleted() throws Exception {
        when(userRepository.findById(author.getUuid())).thenReturn(Optional.of(author));

        userServiceWithMockedDependencies().deleteUser(author.getUuid());

        InOrder order = inOrder(absenceCommentRepository, absenceRepository, userRepository);
        order.verify(absenceCommentRepository).setAuthorToNull(author);
        order.verify(absenceCommentRepository).deleteByAbsenceUser(author);
        order.verify(absenceRepository).deleteByUser(author);
        order.verify(userRepository).delete(author);
    }

    /** UserService compte une vingtaine de dépendances : seules celles vérifiées ici sont nommées. */
    private UserService userServiceWithMockedDependencies() throws Exception {
        Map<Class<?>, Object> known = Map.of(
                AbsenceCommentRepository.class, absenceCommentRepository,
                AbsenceRepository.class, absenceRepository,
                UserRepository.class, userRepository);
        Constructor<?> constructor = UserService.class.getConstructors()[0];
        Object[] dependencies = Arrays.stream(constructor.getParameterTypes())
                .map(type -> known.containsKey(type) ? known.get(type) : mock(type))
                .toArray();
        return (UserService) constructor.newInstance(dependencies);
    }

    private static User buildAdmin(String email, String firstName, String lastName) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }
}
