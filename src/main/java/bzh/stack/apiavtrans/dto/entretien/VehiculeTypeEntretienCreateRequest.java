package bzh.stack.apiavtrans.dto.entretien;

import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création d'une configuration d'entretien pour un véhicule")
public class VehiculeTypeEntretienCreateRequest {

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID vehiculeId;

    @Schema(description = "Identifiant du type d'entretien", example = "123e4567-e89b-12d3-a456-426614174002", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID typeEntretienId;

    @Schema(description = "Type de périodicité", example = "KILOMETRAGE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"KILOMETRAGE", "TEMPOREL"})
    private VehiculeTypeEntretien.PeriodiciteType periodiciteType;

    @Schema(description = "Valeur de la périodicité (km pour KILOMETRAGE, jours pour TEMPOREL)", example = "30000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer periodiciteValeur;
}
