package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de recherche d'historique des entretiens avec pagination et filtres")
public class EntretienHistoryRequest {

    @Schema(description = "Numéro de page (commence à 0)", example = "0")
    private Integer page;

    @Schema(description = "Nombre d'éléments par page", example = "10")
    private Integer size;

    @Schema(description = "Champ de tri (dateEntretien, kilometrage, cout)", example = "dateEntretien")
    private String sortBy;

    @Schema(description = "Direction du tri (asc ou desc)", example = "desc")
    private String sortDirection;

    @Schema(description = "Date de début de recherche", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "Date de fin de recherche", example = "2025-12-31")
    private LocalDate endDate;

    @Schema(description = "Filtrer par véhicule (UUID)")
    private UUID vehiculeId;

    @Schema(description = "Filtrer par type d'entretien (UUID)")
    private UUID typeEntretienId;

    @Schema(description = "Filtrer par dossier de types d'entretien (UUID)")
    private UUID dossierId;

    @Schema(description = "Filtrer par mécanicien (UUID)")
    private UUID mecanicienId;

    @Schema(description = "Kilométrage minimum", example = "50000")
    private Integer kmMin;

    @Schema(description = "Kilométrage maximum", example = "100000")
    private Integer kmMax;

    @Schema(description = "Coût minimum", example = "0")
    private Double coutMin;

    @Schema(description = "Coût maximum", example = "500")
    private Double coutMax;
}
