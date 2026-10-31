package bzh.stack.apiavtrans.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Commentaires internes des absences, réservés aux administrateurs. Seul l'auteur peut modifier ou
 * supprimer son commentaire.
 */
@Service
@RequiredArgsConstructor
public class AbsenceCommentService {

    static final int MAX_CONTENT_LENGTH = 2000;

    private final AbsenceCommentRepository absenceCommentRepository;
    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final AbsenceCommentMapper absenceCommentMapper;

    @Transactional(readOnly = true)
    public AbsenceCommentListResponse getComments(UUID absenceUuid) {
        if (!absenceRepository.existsById(absenceUuid)) {
            throw new RuntimeException("Absence non trouvée");
        }

        List<AbsenceCommentDTO> comments = absenceCommentRepository.findByAbsenceUuidInOrder(absenceUuid).stream()
                .map(absenceCommentMapper::toDTO)
                .toList();

        return new AbsenceCommentListResponse(true, comments);
    }

    @Transactional
    public AbsenceCommentResponse createComment(UUID absenceUuid, String authorEmail, AbsenceCommentCreateRequest request) {
        String contentError = contentError(request.getContent());
        if (contentError != null) {
            return new AbsenceCommentResponse(false, contentError, null);
        }

        User author = findAdmin(authorEmail);
        Absence absence = absenceRepository.findById(absenceUuid)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée"));

        AbsenceComment comment = new AbsenceComment();
        comment.setAbsence(absence);
        comment.setAuthor(author);
        comment.setContent(request.getContent().strip());

        AbsenceComment saved = absenceCommentRepository.save(comment);

        return new AbsenceCommentResponse(true, "Commentaire ajouté", absenceCommentMapper.toDTO(saved));
    }

    @Transactional
    public AbsenceCommentResponse updateComment(UUID commentUuid, String adminEmail, AbsenceCommentUpdateRequest request) {
        String contentError = contentError(request.getContent());
        if (contentError != null) {
            return new AbsenceCommentResponse(false, contentError, null);
        }

        AbsenceComment comment = findComment(commentUuid);
        if (!isAuthor(comment, findAdmin(adminEmail))) {
            return new AbsenceCommentResponse(false, "Vous ne pouvez modifier que vos propres commentaires", null);
        }

        comment.setContent(request.getContent().strip());
        // Flush immédiat : updatedAt n'est généré qu'au flush et la réponse doit porter la nouvelle date
        AbsenceComment saved = absenceCommentRepository.saveAndFlush(comment);

        return new AbsenceCommentResponse(true, "Commentaire modifié", absenceCommentMapper.toDTO(saved));
    }

    @Transactional
    public AbsenceCommentResponse deleteComment(UUID commentUuid, String adminEmail) {
        AbsenceComment comment = findComment(commentUuid);
        if (!isAuthor(comment, findAdmin(adminEmail))) {
            return new AbsenceCommentResponse(false, "Vous ne pouvez supprimer que vos propres commentaires", null);
        }

        absenceCommentRepository.delete(comment);

        return new AbsenceCommentResponse(true, "Commentaire supprimé", null);
    }

    private User findAdmin(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));
    }

    private AbsenceComment findComment(UUID commentUuid) {
        return absenceCommentRepository.findById(commentUuid)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
    }

    /** Un commentaire dont l'auteur a été supprimé n'appartient plus à personne. */
    private boolean isAuthor(AbsenceComment comment, User user) {
        return comment.getAuthor() != null && comment.getAuthor().getUuid().equals(user.getUuid());
    }

    /** Message d'erreur du contenu (mesuré sans les espaces de début et de fin), null s'il est valide. */
    private String contentError(String content) {
        if (content == null || content.isBlank()) {
            return "Le commentaire ne peut pas être vide";
        }
        if (content.strip().length() > MAX_CONTENT_LENGTH) {
            return "Le commentaire ne doit pas dépasser " + MAX_CONTENT_LENGTH + " caractères";
        }
        return null;
    }
}
