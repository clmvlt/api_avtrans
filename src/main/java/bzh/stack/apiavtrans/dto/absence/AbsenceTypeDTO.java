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
public class AbsenceTypeDTO {
    private UUID uuid;
    private String name;
    private String color;
    private ZonedDateTime createdAt;

    @Schema(description = "Mode de décompte : JOURS_OUVRABLES (lun.-sam., 6 j/sem.), JOURS_OUVRES (lun.-ven., 5 j/sem.) "
            + "ou JOURS_CALENDAIRES (7 j/sem.)", example = "JOURS_OUVRABLES")
    private String modeDecompte;

    @Schema(description = "L'absence crédite-t-elle des heures ? (faux pour un congé sans solde)", example = "true")
    private Boolean compteHeures;
}
