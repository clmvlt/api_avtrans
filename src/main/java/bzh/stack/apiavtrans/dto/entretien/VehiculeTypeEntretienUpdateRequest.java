package bzh.stack.apiavtrans.dto.entretien;

import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'une configuration d'entretien")
public class VehiculeTypeEntretienUpdateRequest {

    @Schema(description = "Type de périodicité", example = "KILOMETRAGE", allowableValues = {"KILOMETRAGE", "TEMPOREL"})
    private VehiculeTypeEntretien.PeriodiciteType periodiciteType;

    @Schema(description = "Valeur de la périodicité (km pour KILOMETRAGE, jours pour TEMPOREL)", example = "30000")
    private Integer periodiciteValeur;

    @Schema(description = "Indique si la configuration est active", example = "true")
    private Boolean actif;
}
