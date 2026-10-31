package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCommentResponse {
    private boolean success;
    private String message;

    @Schema(description = "Commentaire créé ou modifié (null après une suppression)")
    private AbsenceCommentDTO comment;
}
