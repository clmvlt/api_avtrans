package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Commentaire interne sur une absence, visible uniquement par les administrateurs")
public class AbsenceCommentDTO {
    @Schema(description = "Identifiant du commentaire")
    private UUID uuid;

    @Schema(description = "Absence commentée")
    private UUID absenceUuid;

    @Schema(description = "Auteur (null si le compte a été supprimé)")
    private UUID authorUuid;

    @Schema(description = "Prénom et nom de l'auteur (null si le compte a été supprimé)", example = "Marie Martin")
    private String authorName;

    @Schema(description = "Texte du commentaire", example = "Justificatif reçu par e-mail.")
    private String content;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
