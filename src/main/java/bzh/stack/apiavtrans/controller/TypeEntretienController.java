package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.entretien.*;
import bzh.stack.apiavtrans.service.TypeEntretienService;
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
@RequestMapping("/types-entretien")
@RequiredArgsConstructor
@Tag(name = "Maintenance Types", description = "Management of maintenance types (brakes, tires, inspections, etc.)")
public class TypeEntretienController {

    private final TypeEntretienService typeEntretienService;

    @Operation(summary = "[MÉCANICIEN] Get all maintenance types")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance types retrieved successfully",
            content = @Content(schema = @Schema(implementation = TypeEntretienListResponse.class))
    )
    @RequireRole("Mécanicien")
    @GetMapping
    public ResponseEntity<?> getAllTypesEntretien() {
        try {
            List<TypeEntretienDTO> types = typeEntretienService.getAllTypesEntretien();
            return ResponseEntity.ok(new TypeEntretienListResponse(true, types));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Get maintenance type by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type retrieved successfully",
            content = @Content(schema = @Schema(implementation = TypeEntretienResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Maintenance type not found")
    @RequireRole("Mécanicien")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTypeEntretienById(@Parameter(description = "Maintenance type UUID") @PathVariable UUID id) {
        try {
            TypeEntretienDTO type = typeEntretienService.getTypeEntretienById(id);
            return ResponseEntity.ok(new TypeEntretienResponse(true, null, type));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Create maintenance type")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type created successfully",
            content = @Content(schema = @Schema(implementation = TypeEntretienResponse.class))
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Maintenance type information to create",
            required = true,
            content = @Content(schema = @Schema(implementation = TypeEntretienCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createTypeEntretien(@RequestBody TypeEntretienCreateRequest request) {
        try {
            TypeEntretienDTO type = typeEntretienService.createTypeEntretien(request);
            return ResponseEntity.ok(new TypeEntretienResponse(true, "Type d'entretien créé avec succès", type));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Update maintenance type")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type updated successfully",
            content = @Content(schema = @Schema(implementation = TypeEntretienResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Maintenance type not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated maintenance type information",
            required = true,
            content = @Content(schema = @Schema(implementation = TypeEntretienUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTypeEntretien(
            @Parameter(description = "Maintenance type UUID") @PathVariable UUID id,
            @RequestBody TypeEntretienUpdateRequest request) {
        try {
            TypeEntretienDTO type = typeEntretienService.updateTypeEntretien(id, request);
            return ResponseEntity.ok(new TypeEntretienResponse(true, "Type d'entretien modifié avec succès", type));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Delete maintenance type")
    @ApiResponse(
            responseCode = "200",
            description = "Maintenance type deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Maintenance type not found")
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTypeEntretien(@Parameter(description = "Maintenance type UUID") @PathVariable UUID id) {
        try {
            typeEntretienService.deleteTypeEntretien(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Type d'entretien supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
