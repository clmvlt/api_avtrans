package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.absence.AbsenceCommentDTO;
import bzh.stack.apiavtrans.entity.AbsenceComment;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AbsenceCommentMapper {

    public AbsenceCommentDTO toDTO(AbsenceComment comment) {
        if (comment == null) {
            return null;
        }

        AbsenceCommentDTO dto = new AbsenceCommentDTO();
        dto.setUuid(comment.getUuid());
        dto.setAbsenceUuid(comment.getAbsence().getUuid());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        User author = comment.getAuthor();
        if (author != null) {
            dto.setAuthorUuid(author.getUuid());
            dto.setAuthorName(authorName(author));
        }
        return dto;
    }

    /** Prénom et nom ; l'e-mail pour un compte sans nom, un nom null signifiant « auteur supprimé ». */
    private String authorName(User author) {
        String firstName = author.getFirstName() != null ? author.getFirstName() : "";
        String lastName = author.getLastName() != null ? author.getLastName() : "";
        String name = (firstName + " " + lastName).trim();
        return name.isEmpty() ? author.getEmail() : name;
    }
}
