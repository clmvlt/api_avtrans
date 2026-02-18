package bzh.stack.apiavtrans.dto.vehicule;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant un relevé de kilométrage")
public class VehiculeKilometrageDTO {

    @Schema(description = "Identifiant unique du relevé", example = "789e0123-e89b-12d3-a456-426614174222")
    private UUID id;

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID vehiculeId;

    @Schema(description = "Kilométrage relevé", example = "125000")
    private Integer km;

    @Schema(description = "Utilisateur ayant effectué le relevé")
    private UserDTO user;

    @Schema(description = "Date du relevé", example = "2025-01-15T14:20:00+01:00")
    private ZonedDateTime createdAt;
}
