package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Équipement d'un véhicule")
public class VehiculeEquipementDTO {

    @Schema(description = "Identifiant unique", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Identifiant du véhicule", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID vehiculeId;

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String vehiculeImmat;

    @Schema(description = "Nom de l'équipement", example = "Gilet jaune")
    private String nom;

    @Schema(description = "Quantité", example = "2")
    private Integer quantite;

    @Schema(description = "Commentaire", example = "À remplacer avant fin mars")
    private String commentaire;

    @Schema(description = "Date de création", example = "2026-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;
}
