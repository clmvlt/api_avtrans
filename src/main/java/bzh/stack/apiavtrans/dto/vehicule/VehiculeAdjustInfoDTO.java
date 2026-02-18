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
@Schema(description = "DTO représentant une information d'ajustement/maintenance")
public class VehiculeAdjustInfoDTO {

    @Schema(description = "Identifiant unique de l'information", example = "abc1234-e89b-12d3-a456-426614174444")
    private UUID id;

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID vehiculeId;

    @Schema(description = "Utilisateur ayant créé l'information")
    private UserDTO user;

    @Schema(description = "Commentaire décrivant l'ajustement", example = "Changement des pneus avant")
    private String comment;

    @Schema(description = "Date de création", example = "2025-01-15T16:00:00+01:00")
    private ZonedDateTime createdAt;
}
