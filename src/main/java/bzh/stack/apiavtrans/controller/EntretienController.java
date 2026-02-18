package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.entretien.*;
import bzh.stack.apiavtrans.entity.Entretien;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.AlerteService;
import bzh.stack.apiavtrans.service.EntretienService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/entretiens")
@RequiredArgsConstructor
@Tag(name = "Entretiens", description = "Complete management of vehicle maintenance records, attached files, and maintenance alerts")
public class EntretienController {

    private final EntretienService entretienService;
    private final AlerteService alerteService;

    // ==================== MAINTENANCE CRUD ====================

    @Operation(
            summary = "[MÉCANICIEN] Create a maintenance record",
            description = "Creates a new maintenance record for a vehicle. Files (PDF, images, documents) can be attached during creation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maintenance record created successfully",
                    content = @Content(schema = @Schema(implementation = EntretienResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle or maintenance type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createEntretien(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Maintenance information with optional file attachments",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EntretienCreateRequest.class))
            )
            @RequestBody EntretienCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User mecanicien = (User) httpRequest.getAttribute("user");
            EntretienDTO entretien = entretienService.createEntretien(request, mecanicien);
            return ResponseEntity.ok(new EntretienResponse(true, "Entretien créé avec succès", entretien));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Get maintenance record by ID",
            description = "Retrieves a specific maintenance record with all its details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maintenance record retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EntretienResponse.class))),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @GetMapping("/{id}")
    public ResponseEntity<?> getEntretienById(
            @Parameter(description = "Maintenance record UUID", required = true)
            @PathVariable UUID id) {
        try {
            EntretienDTO entretien = entretienService.getEntretienById(id);
            return ResponseEntity.ok(new EntretienResponse(true, null, entretien));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Update a maintenance record",
            description = "Updates an existing maintenance record. Only provided fields will be updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maintenance record updated successfully",
                    content = @Content(schema = @Schema(implementation = EntretienResponse.class))),
            @ApiResponse(responseCode = "404", description = "Maintenance record or type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntretien(
            @Parameter(description = "Maintenance record UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EntretienUpdateRequest.class))
            )
            @RequestBody EntretienUpdateRequest request) {
        try {
            EntretienDTO entretien = entretienService.updateEntretien(id, request);
            return ResponseEntity.ok(new EntretienResponse(true, "Entretien modifié avec succès", entretien));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Delete a maintenance record",
            description = "Deletes a maintenance record and all its attached files."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maintenance record deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntretien(
            @Parameter(description = "Maintenance record UUID", required = true)
            @PathVariable UUID id) {
        try {
            entretienService.deleteEntretien(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Entretien supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    // ==================== MAINTENANCE QUERIES ====================

    @Operation(
            summary = "[MÉCANICIEN] Search maintenance records with filters and pagination",
            description = "Advanced search for maintenance records with multiple filter criteria and pagination support."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = EntretienHistoryResponse.class)))
    })
    @RequireRole("Mécanicien")
    @PostMapping("/history")
    public ResponseEntity<?> searchEntretiens(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Search criteria and pagination parameters",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EntretienHistoryRequest.class))
            )
            @RequestBody EntretienHistoryRequest request) {
        try {
            Page<Entretien> entretienPage = entretienService.searchEntretiens(request);

            List<EntretienDTO> entretienDTOs = entretienPage.getContent().stream()
                    .map(entretien -> entretienService.getEntretienById(entretien.getId()))
                    .collect(Collectors.toList());

            EntretienHistoryResponse response = new EntretienHistoryResponse(
                    true,
                    entretienDTOs,
                    entretienPage.getNumber(),
                    entretienPage.getSize(),
                    entretienPage.getTotalElements(),
                    entretienPage.getTotalPages(),
                    entretienPage.isFirst(),
                    entretienPage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    // ==================== FILE MANAGEMENT ====================

    @Operation(
            summary = "[MÉCANICIEN] Get files attached to a maintenance record",
            description = "Retrieves all files (PDF, images, documents) attached to a maintenance record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EntretienFileListResponse.class))),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @GetMapping("/{id}/files")
    public ResponseEntity<?> getEntretienFiles(
            @Parameter(description = "Maintenance record UUID", required = true)
            @PathVariable UUID id) {
        try {
            List<EntretienFileDTO> files = entretienService.getEntretienFiles(id);
            return ResponseEntity.ok(new EntretienFileListResponse(true, files));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Add a file to a maintenance record",
            description = "Uploads and attaches a file to an existing maintenance record. Supports PDF, images, and documents."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File added successfully",
                    content = @Content(schema = @Schema(implementation = EntretienFileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @PostMapping("/{id}/files")
    public ResponseEntity<?> addEntretienFile(
            @Parameter(description = "Maintenance record UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "File data with base64 content, original filename, and MIME type",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EntretienFileUploadRequest.class))
            )
            @RequestBody EntretienFileUploadRequest request) {
        try {
            EntretienFileDTO file = entretienService.addEntretienFile(id, request);
            return ResponseEntity.ok(new EntretienFileResponse(true, "Fichier ajouté avec succès", file));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Delete a file from a maintenance record",
            description = "Removes a file attachment from a maintenance record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteEntretienFile(
            @Parameter(description = "File UUID", required = true)
            @PathVariable UUID fileId) {
        try {
            entretienService.deleteEntretienFile(fileId);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Fichier supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    // ==================== UPCOMING MAINTENANCE ====================

    @Operation(
            summary = "[MÉCANICIEN] Get all vehicles with upcoming maintenance",
            description = "Retrieves a list of all vehicles with their next scheduled maintenance information (by km and by date)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeProchainEntretienListResponse.class)))
    })
    @RequireRole("Mécanicien")
    @GetMapping("/vehicules-prochains-entretiens")
    public ResponseEntity<?> getVehiculesAvecProchainsEntretiens() {
        try {
            List<VehiculeProchainEntretienDTO> vehicules = alerteService.getVehiculesAvecProchainsEntretiens();
            return ResponseEntity.ok(new VehiculeProchainEntretienListResponse(true, vehicules));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Get upcoming maintenance for a specific vehicle",
            description = "Retrieves the next scheduled maintenance information for a specific vehicle (by km and by date)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upcoming maintenance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehiculeProchainEntretienResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireRole("Mécanicien")
    @GetMapping("/vehicule/{vehiculeId}/prochains-entretiens")
    public ResponseEntity<?> getProchainEntretienForVehicule(
            @Parameter(description = "Vehicle UUID", required = true)
            @PathVariable UUID vehiculeId) {
        try {
            VehiculeProchainEntretienDTO vehicule = alerteService.getProchainEntretienForVehicule(vehiculeId);
            return ResponseEntity.ok(new VehiculeProchainEntretienResponse(true, vehicule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
