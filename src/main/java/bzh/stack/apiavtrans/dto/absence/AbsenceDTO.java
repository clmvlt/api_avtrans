package bzh.stack.apiavtrans.dto.absence;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceDTO {
    private UUID uuid;
    private UserDTO user;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private AbsenceTypeDTO absenceType;
    private String customType;
    private String period;
    private String status;
    private UserDTO validatedBy;
    private ZonedDateTime validatedAt;
    private String rejectionReason;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @Schema(description = "Jours décomptés (règle du type d'absence, jours fériés exclus, 0.5 par demi-journée)", example = "6")
    private Double joursDecomptes;

    @Schema(description = "Heures créditées retenues (forcées sinon calculées)", example = "35.0")
    private Double heures;

    @Schema(description = "Heures calculées d'après le contrat, le type et les jours fériés", example = "35.0")
    private Double heuresCalculees;

    @Schema(description = "Heures fixées à la main par un administrateur (null = calcul automatique)")
    private Double heuresForcees;

    @Schema(description = "Mode de décompte : JOURS_OUVRABLES, JOURS_OUVRES ou JOURS_CALENDAIRES", example = "JOURS_OUVRABLES")
    private String modeDecompte;

    @Schema(description = "Le type d'absence crédite-t-il des heures ?", example = "true")
    private Boolean compteHeures;

    @Schema(description = "L'employé a-t-il des heures de contrat renseignées ? (sinon 0 h)", example = "true")
    private Boolean contratRenseigne;
}
