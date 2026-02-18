package bzh.stack.apiavtrans.dto.entretien;

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
@Schema(description = "Entretien effectué sur un véhicule")
public class EntretienDTO {

    @Schema(description = "Identifiant unique de l'entretien", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID vehiculeId;

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String vehiculeImmat;

    @Schema(description = "Type d'entretien")
    private TypeEntretienDTO typeEntretien;

    @Schema(description = "Mécanicien ayant effectué l'entretien")
    private UserDTO mecanicien;

    @Schema(description = "Date de l'entretien", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime dateEntretien;

    @Schema(description = "Kilométrage au moment de l'entretien", example = "77000")
    private Integer kilometrage;

    @Schema(description = "Commentaire sur l'entretien", example = "Remplacement des plaquettes usées")
    private String commentaire;

    @Schema(description = "Coût de l'entretien en euros", example = "250.50")
    private Double cout;

    @Schema(description = "Date de création de l'enregistrement", example = "2024-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;

    @Schema(description = "Fichiers attachés à l'entretien")
    private List<EntretienFileDTO> files;
}
