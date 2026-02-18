package bzh.stack.apiavtrans.dto.entretien;

import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Configuration d'entretien pour un véhicule spécifique")
public class VehiculeTypeEntretienDTO {

    @Schema(description = "Identifiant unique", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID vehiculeId;

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String vehiculeImmat;

    @Schema(description = "Type d'entretien")
    private TypeEntretienDTO typeEntretien;

    @Schema(description = "Type de périodicité", example = "KILOMETRAGE", allowableValues = {"KILOMETRAGE", "TEMPOREL"})
    private VehiculeTypeEntretien.PeriodiciteType periodiciteType;

    @Schema(description = "Valeur de la périodicité (km pour KILOMETRAGE, jours pour TEMPOREL)", example = "30000")
    private Integer periodiciteValeur;

    @Schema(description = "Indique si la configuration est active", example = "true")
    private Boolean actif;

    @Schema(description = "Date de création", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;
}
