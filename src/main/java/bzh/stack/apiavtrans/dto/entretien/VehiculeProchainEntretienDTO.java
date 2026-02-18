package bzh.stack.apiavtrans.dto.entretien;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Véhicule avec ses prochains entretiens à effectuer (par km et par date)")
public class VehiculeProchainEntretienDTO {

    @Schema(description = "Informations du véhicule")
    private VehiculeDTO vehicule;

    @Schema(description = "Prochain entretien basé sur le kilométrage (null si aucun)")
    private AlerteEntretienDTO prochainEntretienKm;

    @Schema(description = "Prochain entretien basé sur la date (null si aucun)")
    private AlerteEntretienDTO prochainEntretienDate;
}
