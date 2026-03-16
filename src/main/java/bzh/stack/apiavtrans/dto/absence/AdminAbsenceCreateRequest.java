package bzh.stack.apiavtrans.dto.absence;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAbsenceCreateRequest {

    @NotNull(message = "L'UUID de l'utilisateur est obligatoire")
    private UUID userUuid;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    private String reason;

    private UUID absenceTypeUuid;

    private String customType;

    private String period;

    private Boolean approved;
}
