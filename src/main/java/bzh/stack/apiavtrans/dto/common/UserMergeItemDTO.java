package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nombre de données d'un type transférées au profil conservé")
public class UserMergeItemDTO {
    @Schema(description = "Type de donnée", example = "POINTAGES")
    private String type;

    @Schema(description = "Libellé du type", example = "Pointages")
    private String libelle;

    @Schema(description = "Nombre d'éléments transférés", example = "42")
    private Long nombre;
}
