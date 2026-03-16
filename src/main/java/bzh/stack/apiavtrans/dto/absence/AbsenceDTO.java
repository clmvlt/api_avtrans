package bzh.stack.apiavtrans.dto.absence;

import bzh.stack.apiavtrans.dto.common.UserDTO;
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
}
