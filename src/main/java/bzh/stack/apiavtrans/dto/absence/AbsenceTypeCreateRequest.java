package bzh.stack.apiavtrans.dto.absence;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceTypeCreateRequest {

    @NotBlank(message = "Le nom du type est obligatoire")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "La couleur doit être au format hexadécimal (#RRGGBB)")
    private String color;

    @Schema(description = "Mode de décompte : JOURS_OUVRABLES, JOURS_OUVRES ou JOURS_CALENDAIRES "
            + "(création : JOURS_OUVRABLES par défaut ; modification : inchangé si absent)", example = "JOURS_OUVRABLES")
    private String modeDecompte;

    @Schema(description = "L'absence crédite-t-elle des heures ? (création : vrai par défaut ; modification : inchangé si absent)",
            example = "true")
    private Boolean compteHeures;
}
