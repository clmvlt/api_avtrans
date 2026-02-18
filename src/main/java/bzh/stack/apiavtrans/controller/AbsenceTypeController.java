package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.absence.*;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.service.AbsenceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/absence-types")
@RequiredArgsConstructor
@Tag(name = "Absence Types", description = "Absence type management")
public class AbsenceTypeController {

    private final AbsenceTypeService absenceTypeService;

    @Operation(summary = "[UTILISATEUR] Get all absence types")
    @ApiResponse(
            responseCode = "200",
            description = "Absence types retrieved successfully",
            content = @Content(schema = @Schema(implementation = AbsenceTypeListResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping
    public ResponseEntity<?> getAllTypes() {
        try {
            AbsenceTypeListResponse response = absenceTypeService.getAllTypes();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Create a new absence type")
    @ApiResponse(
            responseCode = "200",
            description = "Absence type created successfully",
            content = @Content(schema = @Schema(implementation = AbsenceTypeResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping
    public ResponseEntity<?> createType(@Valid @RequestBody AbsenceTypeCreateRequest request) {
        try {
            AbsenceTypeResponse response = absenceTypeService.createType(request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Update an absence type")
    @ApiResponse(
            responseCode = "200",
            description = "Absence type updated successfully",
            content = @Content(schema = @Schema(implementation = AbsenceTypeResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Absence type not found"
    )
    @RequireRole("Administrateur")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateType(
            @Parameter(description = "Absence type UUID") @PathVariable UUID uuid,
            @Valid @RequestBody AbsenceTypeCreateRequest request) {
        try {
            AbsenceTypeResponse response = absenceTypeService.updateType(uuid, request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Delete an absence type")
    @ApiResponse(
            responseCode = "200",
            description = "Absence type deleted successfully",
            content = @Content(schema = @Schema(implementation = AbsenceTypeResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Absence type not found"
    )
    @RequireRole("Administrateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteType(
            @Parameter(description = "Absence type UUID") @PathVariable UUID uuid) {
        try {
            AbsenceTypeResponse response = absenceTypeService.deleteType(uuid);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get an absence type by UUID")
    @ApiResponse(
            responseCode = "200",
            description = "Absence type retrieved successfully",
            content = @Content(schema = @Schema(implementation = AbsenceTypeResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Absence type not found"
    )
    @RequireRole("Utilisateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getTypeById(
            @Parameter(description = "Absence type UUID") @PathVariable UUID uuid) {
        try {
            AbsenceTypeResponse response = absenceTypeService.getTypeById(uuid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
