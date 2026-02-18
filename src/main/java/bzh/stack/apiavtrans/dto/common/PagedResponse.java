package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse paginée générique")
public class PagedResponse<T> {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Contenu de la page")
    private List<T> content;

    @Schema(description = "Numéro de la page (commence à 0)", example = "0")
    private Integer page;

    @Schema(description = "Taille de la page", example = "10")
    private Integer size;

    @Schema(description = "Nombre total d'éléments", example = "100")
    private Long totalElements;

    @Schema(description = "Nombre total de pages", example = "10")
    private Integer totalPages;

    @Schema(description = "Indique si c'est la première page", example = "true")
    private Boolean first;

    @Schema(description = "Indique si c'est la dernière page", example = "false")
    private Boolean last;
}
