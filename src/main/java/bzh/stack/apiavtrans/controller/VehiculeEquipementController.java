package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.vehicule.*;
import bzh.stack.apiavtrans.service.VehiculeEquipementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicules-equipements")
@RequiredArgsConstructor
@Tag(name = "Vehicle Equipments", description = "Gestion des équipements des véhicules")
public class VehiculeEquipementController {

    private final VehiculeEquipementService vehiculeEquipementService;

    @Operation(summary = "[UTILISATEUR] Récupérer les équipements d'un véhicule")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Équipements récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeEquipementListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Véhicule non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Utilisateur")
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<?> getEquipementsByVehicule(
            @Parameter(description = "UUID du véhicule") @PathVariable UUID vehiculeId) {
        try {
            List<VehiculeEquipementDTO> equipements = vehiculeEquipementService.getEquipementsByVehicule(vehiculeId);
            return ResponseEntity.ok(new VehiculeEquipementListResponse(true, equipements));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Récupérer un équipement par ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Équipement récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeEquipementResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Équipement non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Utilisateur")
    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipementById(
            @Parameter(description = "UUID de l'équipement") @PathVariable UUID id) {
        try {
            VehiculeEquipementDTO equipement = vehiculeEquipementService.getEquipementById(id);
            return ResponseEntity.ok(new VehiculeEquipementResponse(true, null, equipement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Créer un équipement pour un véhicule")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Équipement créé avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeEquipementResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Détails de l'équipement à créer",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeEquipementCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createEquipement(@RequestBody VehiculeEquipementCreateRequest request) {
        try {
            VehiculeEquipementDTO equipement = vehiculeEquipementService.createEquipement(request);
            return ResponseEntity.ok(new VehiculeEquipementResponse(true, "Équipement créé avec succès", equipement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Modifier un équipement")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Équipement modifié avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeEquipementResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Équipement non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Détails de l'équipement à modifier",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeEquipementUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEquipement(
            @Parameter(description = "UUID de l'équipement") @PathVariable UUID id,
            @RequestBody VehiculeEquipementUpdateRequest request) {
        try {
            VehiculeEquipementDTO equipement = vehiculeEquipementService.updateEquipement(id, request);
            return ResponseEntity.ok(new VehiculeEquipementResponse(true, "Équipement modifié avec succès", equipement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Supprimer un équipement")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Équipement supprimé avec succès",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Équipement non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEquipement(
            @Parameter(description = "UUID de l'équipement") @PathVariable UUID id) {
        try {
            vehiculeEquipementService.deleteEquipement(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Équipement supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
