package bzh.stack.apiavtrans.dto.entretien;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeDTO;
import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alerte d'entretien à effectuer")
public class AlerteEntretienDTO {

    @Schema(description = "Informations du véhicule")
    private VehiculeDTO vehicule;

    @Schema(description = "Type d'entretien")
    private TypeEntretienDTO typeEntretien;

    @Schema(description = "Dernier entretien effectué")
    private EntretienDTO dernierEntretien;

    @Schema(description = "Nature de la récurrence", example = "KILOMETRAGE")
    private VehiculeTypeEntretien.PeriodiciteType periodiciteType;

    @Schema(description = "Valeur de la récurrence (km ou jours selon la nature)", example = "30000")
    private Integer periodiciteValeur;

    @Schema(description = "Kilométrage prévu du prochain entretien", example = "107000")
    private Integer prochainKilometrage;

    @Schema(description = "Date prévue du prochain entretien (pour entretiens temporels)", example = "2026-01-15T10:30:00+01:00")
    private ZonedDateTime prochaineDateTemporelle;

    @Schema(description = "Nombre de km restants avant l'entretien", example = "5000")
    private Integer kmRestants;

    @Schema(description = "Jours calendaires restants à Paris avant l'entretien (0 = aujourd'hui, négatif = dépassé)", example = "365")
    private Long joursRestants;

    @Schema(description = "Indique si l'entretien est en retard", example = "false")
    private Boolean enRetard;

    @Schema(description = "Message d'alerte", example = "Entretien à prévoir dans 5000 km")
    private String message;
}
