package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCommentListResponse {
    private boolean success;

    @Schema(description = "Commentaires de l'absence, du plus ancien au plus récent")
    private List<AbsenceCommentDTO> comments;
}
