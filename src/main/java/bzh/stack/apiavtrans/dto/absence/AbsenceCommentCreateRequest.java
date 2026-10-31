package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCommentCreateRequest {
    @Schema(description = "Texte du commentaire, sans les espaces de début et de fin : non vide, 2000 caractères au plus",
            example = "Justificatif reçu par e-mail.")
    private String content;
}
