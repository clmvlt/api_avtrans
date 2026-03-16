package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour modifier un véhicule existant")
public class VehiculeUpdateRequest {

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String immat;

    @Schema(description = "Modèle du véhicule", example = "Transit Custom")
    private String model;

    @Schema(description = "Marque du véhicule", example = "Ford")
    private String brand;

    @Schema(description = "Commentaire optionnel", example = "Véhicule de livraison principal - Révisé")
    private String comment;

    @Schema(description = "Photo de profil du véhicule en base64", example = "data:image/png;base64,iVBORw0KGgo...", nullable = true)
    private String pictureBase64;

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
