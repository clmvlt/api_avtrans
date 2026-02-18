package bzh.stack.apiavtrans.dto.acompte;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteAdminCreateRequest {
    @NotNull(message = "L'utilisateur est obligatoire")
    private UUID userUuid;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Double montant;

    private String raison;

    private Boolean approved;
}
