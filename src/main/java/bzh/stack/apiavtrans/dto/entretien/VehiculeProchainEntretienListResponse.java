package bzh.stack.apiavtrans.dto.entretien;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant la liste des véhicules avec leurs prochains entretiens")
public class VehiculeProchainEntretienListResponse {

    @Schema(description = "Indique si la requête a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Liste des véhicules avec leurs prochains entretiens")
    private List<VehiculeProchainEntretienDTO> data;
}
