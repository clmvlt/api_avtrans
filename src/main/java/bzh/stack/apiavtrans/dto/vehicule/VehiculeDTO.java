package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Schema(description = "Numéro de série / VIN du véhicule", example = "WF0XXXGCDX1234567", nullable = true)
    private String vin;

    @Schema(description = "Numéro du certificat d'immatriculation (carte grise)", example = "2024AB12345", nullable = true)
    private String numeroCarteGrise;

    @Schema(description = "Date de première mise en circulation", example = "2020-06-15", nullable = true)
    private LocalDate dateMiseEnCirculation;

    @Schema(description = "Type de carburant", example = "Diesel", nullable = true)
    private String typeCarburant;

    @Schema(description = "Poids Total Autorisé en Charge en kg", example = "3500", nullable = true)
    private Integer ptac;

    @Schema(description = "Numéro du contrat d'assurance", example = "ASS-2025-123456", nullable = true)
    private String numeroContratAssurance;

    @Schema(description = "Nom de la compagnie d'assurance", example = "AXA", nullable = true)
    private String assureur;

    @Schema(description = "Date d'expiration de l'assurance", example = "2026-12-31", nullable = true)
    private LocalDate dateExpirationAssurance;

    @Schema(description = "Date du prochain contrôle technique", example = "2026-06-15", nullable = true)
    private LocalDate dateProchainControleTechnique;
}
