package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fusion de deux profils : le profil supprimé cède toutes ses données au profil conservé (celui de l'URL)")
public class MergeUsersRequest {
    @Schema(description = "UUID du profil à supprimer après transfert de ses données", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID sourceUuid;
}
