package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour enregistrer un nouveau kilométrage")
public class VehiculeKilometrageCreateRequest {

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID vehiculeId;

    @Schema(description = "Kilométrage à enregistrer", example = "125000")
    private Integer km;
}
