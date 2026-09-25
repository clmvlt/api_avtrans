package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtres pour le journal des actions administrateur sur les pointages")
public class ServiceModificationSearchRequest {

    @Schema(description = "Filtrer sur l'utilisateur dont le pointage a été touché")
    private UUID userUuid;

    @Schema(description = "Filtrer sur l'administrateur auteur de l'action")
    private UUID modifiedByUuid;

    @Schema(description = "Filtrer par type d'action : CREATE, UPDATE ou DELETE", example = "UPDATE")
    private String action;

    @Schema(description = "Actions faites à partir de cette date (incluse)", example = "2026-09-01")
    private LocalDate startDate;

    @Schema(description = "Actions faites jusqu'à cette date (incluse)", example = "2026-09-30")
    private LocalDate endDate;

    @Schema(description = "Numéro de page (commence à 0)", example = "0")
    private Integer page = 0;

    @Schema(description = "Taille de la page", example = "20")
    private Integer size = 20;
}
