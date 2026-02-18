package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.entretien.*;
import bzh.stack.apiavtrans.service.DossierTypeEntretienService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dossiers-types-entretien")
@RequiredArgsConstructor
@Tag(name = "Maintenance Type Folders", description = "Management of maintenance type folders (categories)")
public class DossierTypeEntretienController {

    private final DossierTypeEntretienService dossierTypeEntretienService;

    @Operation(summary = "[MÉCANICIEN] Get all maintenance type folders")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type folders retrieved successfully",
            content = @Content(schema = @Schema(implementation = DossierTypeEntretienListResponse.class))
    )
    @RequireRole("Mécanicien")
    @GetMapping
    public ResponseEntity<?> getAllDossiers() {
        try {
            List<DossierTypeEntretienDTO> dossiers = dossierTypeEntretienService.getAllDossiers();
            return ResponseEntity.ok(new DossierTypeEntretienListResponse(true, dossiers));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Get maintenance type folder by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type folder retrieved successfully",
            content = @Content(schema = @Schema(implementation = DossierTypeEntretienResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Maintenance type folder not found")
    @RequireRole("Mécanicien")
    @GetMapping("/{id}")
    public ResponseEntity<?> getDossierById(@Parameter(description = "Maintenance type folder UUID") @PathVariable UUID id) {
        try {
            DossierTypeEntretienDTO dossier = dossierTypeEntretienService.getDossierById(id);
            return ResponseEntity.ok(new DossierTypeEntretienResponse(true, null, dossier));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Create maintenance type folder")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type folder created successfully",
            content = @Content(schema = @Schema(implementation = DossierTypeEntretienResponse.class))
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Maintenance type folder information to create",
            required = true,
            content = @Content(schema = @Schema(implementation = DossierTypeEntretienCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createDossier(@RequestBody DossierTypeEntretienCreateRequest request) {
        try {
            DossierTypeEntretienDTO dossier = dossierTypeEntretienService.createDossier(request);
            return ResponseEntity.ok(new DossierTypeEntretienResponse(true, "Dossier créé avec succès", dossier));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Update maintenance type folder")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type folder updated successfully",
            content = @Content(schema = @Schema(implementation = DossierTypeEntretienResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Maintenance type folder not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated maintenance type folder information",
            required = true,
            content = @Content(schema = @Schema(implementation = DossierTypeEntretienUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDossier(
            @Parameter(description = "Maintenance type folder UUID") @PathVariable UUID id,
            @RequestBody DossierTypeEntretienUpdateRequest request) {
        try {
            DossierTypeEntretienDTO dossier = dossierTypeEntretienService.updateDossier(id, request);
            return ResponseEntity.ok(new DossierTypeEntretienResponse(true, "Dossier modifié avec succès", dossier));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Delete maintenance type folder")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type folder deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Maintenance type folder not found")
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDossier(@Parameter(description = "Maintenance type folder UUID") @PathVariable UUID id) {
        try {
            dossierTypeEntretienService.deleteDossier(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Dossier supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
