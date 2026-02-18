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
@Schema(description = "DTO représentant un véhicule avec ses informations de base et le kilométrage le plus récent")
public class VehiculeDTO {

    @Schema(description = "Identifiant unique du véhicule", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String immat;

    @Schema(description = "Date de création du véhicule", example = "2025-01-15T10:30:00+01:00")
    private ZonedDateTime createdAt;

    @Schema(description = "Modèle du véhicule", example = "Transit")
    private String model;

    @Schema(description = "Marque du véhicule", example = "Ford")
    private String brand;

    @Schema(description = "Commentaire optionnel", example = "Véhicule de livraison principal")
    private String comment;

    @Schema(description = "Kilométrage le plus récent enregistré", example = "125000", nullable = true)
    private Integer latestKm;

    @Schema(description = "Date du kilométrage le plus récent", example = "2025-01-15T14:20:00+01:00", nullable = true)
    private ZonedDateTime latestKmDate;

    @Schema(description = "URL de la photo de profil du véhicule", example = "http://192.168.1.120:8081/uploads/vehicules/profile/abc123.jpg", nullable = true)
    private String pictureUrl;
}
