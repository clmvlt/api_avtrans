package bzh.stack.apiavtrans.dto.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour récupérer les services d'un utilisateur (admin)")
public class AdminServiceSearchRequest {

    @Schema(description = "Numéro de page (commence à 0)", example = "0")
    private Integer page = 0;

    @Schema(description = "Taille de la page", example = "20")
    private Integer size = 20;

    @Schema(description = "Filtrer par type : true = pauses, false = services, null = tous", example = "false")
    private Boolean isBreak;

    @Schema(description = "Date de début - si seul, recherche depuis cette date jusqu'à maintenant (par défaut : 30 jours avant)", example = "2025-01-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startDate;

    @Schema(description = "Date de fin (par défaut : maintenant)", example = "2025-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endDate;

    @Schema(description = "Champ de tri (debut, fin, duree)", example = "debut")
    private String sortBy = "debut";

    @Schema(description = "Direction du tri (asc, desc)", example = "desc")
    private String sortDirection = "desc";
}
