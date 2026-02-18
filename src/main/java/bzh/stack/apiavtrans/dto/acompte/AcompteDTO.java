package bzh.stack.apiavtrans.dto.acompte;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteDTO {
    private UUID uuid;
    private UUID userUuid;
    private UserDTO user;
    private Double montant;
    private String raison;
    private String status;
    private UUID validatedByUuid;
    private UserDTO validatedBy;
    private ZonedDateTime validatedAt;
    private String rejectionReason;
    private Boolean isPaid;
    private ZonedDateTime paidDate;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
