package bzh.stack.apiavtrans.dto.vehicule;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant un rapport de véhicule")
public class RapportVehiculeDTO {

    @Schema(description = "Identifiant unique du rapport", example = "789e0123-e89b-12d3-a456-426614174222")
    private UUID id;

    @Schema(description = "Utilisateur ayant créé le rapport")
    private UserDTO user;

    @Schema(description = "Véhicule concerné par le rapport")
    private VehiculeDTO vehicule;

    @Schema(description = "Commentaire du rapport", example = "Véhicule en bon état général")
    private String commentaire;

    @Schema(description = "Date de création du rapport", example = "2025-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;

    @Schema(description = "Liste des photos associées au rapport")
    private List<RapportVehiculePictureDTO> pictures;
}
