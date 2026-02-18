package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.entretien.*;
import bzh.stack.apiavtrans.service.VehiculeTypeEntretienService;
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
@RequestMapping("/vehicules-types-entretien")
@RequiredArgsConstructor
@Tag(name = "Vehicle Maintenance Configurations", description = "Management of vehicle-specific maintenance configurations")
public class VehiculeTypeEntretienController {

    private final VehiculeTypeEntretienService vehiculeTypeEntretienService;

    @Operation(summary = "[MÉCANICIEN] Get maintenance configurations by vehicle")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configurations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<?> getConfigurationsByVehicule(
            @Parameter(description = "Vehicle UUID") @PathVariable UUID vehiculeId) {
        try {
            List<VehiculeTypeEntretienDTO> configs = vehiculeTypeEntretienService.getConfigurationsByVehicule(vehiculeId);
            return ResponseEntity.ok(new VehiculeTypeEntretienListResponse(true, configs));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Get active maintenance configurations by vehicle")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active configurations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @GetMapping("/vehicule/{vehiculeId}/actives")
    public ResponseEntity<?> getActiveConfigurationsByVehicule(
            @Parameter(description = "Vehicle UUID") @PathVariable UUID vehiculeId) {
        try {
            List<VehiculeTypeEntretienDTO> configs = vehiculeTypeEntretienService.getActiveConfigurationsByVehicule(vehiculeId);
            return ResponseEntity.ok(new VehiculeTypeEntretienListResponse(true, configs));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Get maintenance configuration by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @GetMapping("/{id}")
    public ResponseEntity<?> getConfigurationById(
            @Parameter(description = "Configuration UUID") @PathVariable UUID id) {
        try {
            VehiculeTypeEntretienDTO config = vehiculeTypeEntretienService.getConfigurationById(id);
            return ResponseEntity.ok(new VehiculeTypeEntretienResponse(true, null, config));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Create maintenance configuration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration created successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or configuration already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Configuration details to create",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createConfiguration(@RequestBody VehiculeTypeEntretienCreateRequest request) {
        try {
            VehiculeTypeEntretienDTO config = vehiculeTypeEntretienService.createConfiguration(request);
            return ResponseEntity.ok(new VehiculeTypeEntretienResponse(true, "Configuration créée avec succès", config));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Update maintenance configuration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated configuration details",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeTypeEntretienUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConfiguration(
            @Parameter(description = "Configuration UUID") @PathVariable UUID id,
            @RequestBody VehiculeTypeEntretienUpdateRequest request) {
        try {
            VehiculeTypeEntretienDTO config = vehiculeTypeEntretienService.updateConfiguration(id, request);
            return ResponseEntity.ok(new VehiculeTypeEntretienResponse(true, "Configuration modifiée avec succès", config));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Delete maintenance configuration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConfiguration(
            @Parameter(description = "Configuration UUID") @PathVariable UUID id) {
        try {
            vehiculeTypeEntretienService.deleteConfiguration(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Configuration supprimée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
