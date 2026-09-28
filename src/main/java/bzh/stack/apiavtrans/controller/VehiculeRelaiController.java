package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.vehicule.*;
import bzh.stack.apiavtrans.service.VehiculeRelaiService;
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
@RequestMapping("/vehicules-relais")
@RequiredArgsConstructor
@Tag(name = "Vehicle Relays", description = "Véhicules relais qui remplacent temporairement un véhicule (garage, panne...), avec historique")
public class VehiculeRelaiController {

    private final VehiculeRelaiService vehiculeRelaiService;

    @Operation(
            summary = "[UTILISATEUR] Historique des relais d'un véhicule",
            description = "Relais du véhicule, du plus récent au plus ancien, avec leur statut du jour et leurs relevés kilométriques."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relais récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeRelaiListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Véhicule non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Utilisateur")
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<?> getRelaisByVehicule(
            @Parameter(description = "UUID du véhicule") @PathVariable UUID vehiculeId) {
        try {
            List<VehiculeRelaiDTO> relais = vehiculeRelaiService.getRelaisByVehicule(vehiculeId);
            return ResponseEntity.ok(new VehiculeRelaiListResponse(true, relais));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Récupérer un relais par ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relais récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeRelaiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Relais non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Utilisateur")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRelaiById(
            @Parameter(description = "UUID du relais") @PathVariable UUID id) {
        try {
            VehiculeRelaiDTO relai = vehiculeRelaiService.getRelaiById(id);
            return ResponseEntity.ok(new VehiculeRelaiResponse(true, null, relai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Déclarer un véhicule relais",
            description = "Les relevés kilométriques du véhicule déjà saisis pendant la période sont rattachés au relais. Refusé si la période chevauche un autre relais du véhicule."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relais déclaré avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeRelaiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou période déjà couverte par un autre relais",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Relais à déclarer",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeRelaiCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createRelai(@RequestBody VehiculeRelaiCreateRequest request) {
        try {
            VehiculeRelaiDTO relai = vehiculeRelaiService.createRelai(request);
            return ResponseEntity.ok(new VehiculeRelaiResponse(true, "Relais déclaré avec succès", relai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Modifier ou terminer un relais",
            description = "Remplacement complet : un champ absent est effacé. Renseigner la date de fin et le km au retour termine le relais. Les relevés du véhicule sont rattachés de nouveau selon la période."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relais modifié avec succès",
                    content = @Content(schema = @Schema(implementation = VehiculeRelaiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Relais non trouvé, données invalides ou période déjà couverte par un autre relais",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Relais complet",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeRelaiUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRelai(
            @Parameter(description = "UUID du relais") @PathVariable UUID id,
            @RequestBody VehiculeRelaiUpdateRequest request) {
        try {
            VehiculeRelaiDTO relai = vehiculeRelaiService.updateRelai(id, request);
            return ResponseEntity.ok(new VehiculeRelaiResponse(true, "Relais modifié avec succès", relai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Supprimer un relais",
            description = "Les relevés kilométriques rattachés au relais redeviennent des relevés du véhicule."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relais supprimé avec succès",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Relais non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRelai(
            @Parameter(description = "UUID du relais") @PathVariable UUID id) {
        try {
            vehiculeRelaiService.deleteRelai(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Relais supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
