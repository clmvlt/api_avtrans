package bzh.stack.apiavtrans.dto.entretien;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Véhicule avec tous ses prochains entretiens à effectuer")
public class VehiculeProchainEntretienDTO {

    @Schema(description = "Informations du véhicule")
    private VehiculeDTO vehicule;

    @Schema(description = "Échéance kilométrique la plus proche (null si aucune)")
    private AlerteEntretienDTO prochainEntretienKm;

    @Schema(description = "Échéance par date la plus proche (null si aucune)")
    private AlerteEntretienDTO prochainEntretienDate;

    @Schema(description = "Toutes les échéances des récurrences actives, de la plus urgente à la moins urgente")
    private List<AlerteEntretienDTO> alertes = new ArrayList<>();
}
