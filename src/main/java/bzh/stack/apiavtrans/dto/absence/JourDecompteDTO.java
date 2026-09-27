package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Un jour du décompte d'une absence")
public class JourDecompteDTO {

    @Schema(description = "Date", example = "2026-10-10")
    private LocalDate date;

    @Schema(description = "Part du jour décomptée : 0 (non décompté), 0.5 (demi-journée) ou 1", example = "1")
    private Double fraction;

    @Schema(description = "Heures créditées ce jour (calcul automatique)", example = "5.83")
    private Double heures;

    @Schema(description = "Raison d'exclusion (DIMANCHE, SAMEDI, FERIE) ou SAMEDI_REPRISE pour le samedi ajouté "
            + "(veille de la reprise, jours ouvrables) ; null si le jour est décompté normalement",
            example = "SAMEDI_REPRISE")
    private String motif;

    @Schema(description = "Nom du jour férié, le cas échéant", example = "Toussaint")
    private String ferie;
}
