package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour créer une information d'ajustement avec photos optionnelles")
public class VehiculeAdjustInfoCreateRequest {

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID vehiculeId;

    @Schema(description = "Commentaire décrivant l'ajustement", example = "Changement des pneus avant")
    private String comment;

    @Schema(description = "Liste de photos encodées en base64 (optionnel)", example = "[\"data:image/jpeg;base64,/9j/4AAQSkZJRg...\"]")
    private List<String> picturesB64;
}
