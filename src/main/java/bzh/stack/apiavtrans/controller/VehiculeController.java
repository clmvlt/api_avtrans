package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.vehicule.*;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicules")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Complete management of vehicles, files, mileage, and adjustment information")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @Operation(summary = "[UTILISATEUR] Get all vehicles")
    @ApiResponse(
            responseCode = "200",
            description = "List of vehicles retrieved successfully",
            content = @Content(schema = @Schema(implementation = VehiculeListResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping
    public ResponseEntity<?> getAllVehicules() {
        try {
            List<VehiculeDTO> vehicules = vehiculeService.getAllVehicules();
            return ResponseEntity.ok(new VehiculeListResponse(true, vehicules));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get vehicle by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Vehicle retrieved successfully",
            content = @Content(schema = @Schema(implementation = VehiculeResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @RequireRole("Utilisateur")
    @GetMapping("/{id}")
    public ResponseEntity<?> getVehiculeById(@PathVariable UUID id) {
        try {
            VehiculeDTO vehicule = vehiculeService.getVehiculeById(id);
            return ResponseEntity.ok(new VehiculeResponse(true, null, vehicule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Create vehicle")
    @ApiResponse(
            responseCode = "200",
            description = "Vehicle created successfully",
            content = @Content(schema = @Schema(implementation = VehiculeResponse.class))
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Vehicle information to create",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createVehicule(@RequestBody VehiculeCreateRequest request) {
        try {
            VehiculeDTO vehicule = vehiculeService.createVehicule(request);
            return ResponseEntity.ok(new VehiculeResponse(true, "Véhicule créé avec succès", vehicule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Update vehicle")
    @ApiResponse(
            responseCode = "200",
            description = "Vehicle updated successfully",
            content = @Content(schema = @Schema(implementation = VehiculeResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated vehicle information",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicule(
            @PathVariable UUID id,
            @RequestBody VehiculeUpdateRequest request) {
        try {
            VehiculeDTO vehicule = vehiculeService.updateVehicule(id, request);
            return ResponseEntity.ok(new VehiculeResponse(true, "Véhicule modifié avec succès", vehicule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Delete vehicle")
    @ApiResponse(
            responseCode = "200",
            description = "Vehicle deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicule(@PathVariable UUID id) {
        try {
            vehiculeService.deleteVehicule(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Véhicule supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get vehicle files",
            description = "Retrieves all files (PDF, images, documents) attached to a vehicle.")
    @ApiResponse(
            responseCode = "200",
            description = "Files retrieved successfully",
            content = @Content(schema = @Schema(implementation = VehiculeFileListResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @RequireRole("Utilisateur")
    @GetMapping("/{id}/files")
    public ResponseEntity<?> getVehiculeFiles(@PathVariable UUID id) {
        try {
            List<VehiculeFileDTO> files = vehiculeService.getVehiculeFiles(id);
            return ResponseEntity.ok(new VehiculeFileListResponse(true, files));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Add file to vehicle",
            description = "Uploads and attaches a file to an existing vehicle. Supports PDF, images, and documents.")
    @ApiResponse(
            responseCode = "200",
            description = "File added successfully",
            content = @Content(schema = @Schema(implementation = VehiculeFileResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "File upload request with Base64 content, original name, and MIME type",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeFileUploadRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping("/{id}/files")
    public ResponseEntity<?> addVehiculeFile(
            @PathVariable UUID id,
            @RequestBody VehiculeFileUploadRequest request) {
        try {
            VehiculeFileDTO file = vehiculeService.addVehiculeFile(id, request);
            return ResponseEntity.ok(new VehiculeFileResponse(true, "Fichier ajouté avec succès", file));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[MÉCANICIEN] Delete vehicle file",
            description = "Removes a file attachment from a vehicle.")
    @ApiResponse(
            responseCode = "200",
            description = "File deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @RequireRole("Mécanicien")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteVehiculeFile(@PathVariable UUID fileId) {
        try {
            vehiculeService.deleteVehiculeFile(fileId);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Fichier supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get vehicle mileage history (paginated)",
            description = "Retrieves paginated mileage history for a vehicle. Use size=-1 to retrieve all records without pagination."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Mileage history retrieved successfully",
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageListResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @RequireRole("Utilisateur")
    @GetMapping("/{id}/kilometrages")
    public ResponseEntity<?> getVehiculeKilometrages(
            @PathVariable UUID id,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 300). Use -1 to retrieve all records.", example = "300")
            @RequestParam(defaultValue = "300") int size) {
        try {
            boolean fetchAll = (size == -1);
            if (!fetchAll && size > 300) {
                size = 300;
            }
            Pageable pageable = fetchAll ? Pageable.unpaged() : PageRequest.of(page, size);
            VehiculeKilometrageListResponse response = vehiculeService.getVehiculeKilometrages(id, pageable, fetchAll);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Add vehicle mileage")
    @ApiResponse(
            responseCode = "200",
            description = "Mileage added successfully",
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Mileage information to record",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageCreateRequest.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/kilometrages")
    public ResponseEntity<?> addVehiculeKilometrage(
            @RequestBody VehiculeKilometrageCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            VehiculeKilometrageDTO kilometrage = vehiculeService.addVehiculeKilometrage(request, user);
            return ResponseEntity.ok(new VehiculeKilometrageResponse(true, "Kilométrage ajouté avec succès", kilometrage));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Add vehicle mileage with custom date/time")
    @ApiResponse(
            responseCode = "200",
            description = "Mileage added successfully",
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle or user not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Mileage information with optional custom date/time and user",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageAdminCreateRequest.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/kilometrages")
    public ResponseEntity<?> addVehiculeKilometrageAsAdmin(
            @RequestBody VehiculeKilometrageAdminCreateRequest request) {
        try {
            VehiculeKilometrageDTO kilometrage = vehiculeService.addVehiculeKilometrageAsAdmin(request);
            return ResponseEntity.ok(new VehiculeKilometrageResponse(true, "Kilométrage ajouté avec succès", kilometrage));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Update vehicle mileage")
    @ApiResponse(
            responseCode = "200",
            description = "Mileage updated successfully",
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Mileage record or user not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated mileage information",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeKilometrageUpdateRequest.class))
    )
    @RequireRole("Administrateur")
    @PutMapping("/admin/kilometrages/{id}")
    public ResponseEntity<?> updateVehiculeKilometrage(
            @Parameter(description = "Mileage record UUID") @PathVariable UUID id,
            @RequestBody VehiculeKilometrageUpdateRequest request) {
        try {
            VehiculeKilometrageDTO kilometrage = vehiculeService.updateVehiculeKilometrage(id, request);
            return ResponseEntity.ok(new VehiculeKilometrageResponse(true, "Kilométrage modifié avec succès", kilometrage));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get vehicle adjustment information (paginated)")
    @ApiResponse(
            responseCode = "200",
            description = "Adjustment information retrieved successfully",
            content = @Content(schema = @Schema(implementation = VehiculeAdjustInfoListResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @RequireRole("Utilisateur")
    @GetMapping("/{id}/adjust-infos")
    public ResponseEntity<?> getVehiculeAdjustInfos(
            @PathVariable UUID id,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        try {
            if (size > 50) {
                size = 50;
            }
            Pageable pageable = PageRequest.of(page, size);
            VehiculeAdjustInfoListResponse response = vehiculeService.getVehiculeAdjustInfos(id, pageable);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Create vehicle adjustment information")
    @ApiResponse(
            responseCode = "200",
            description = "Adjustment information created successfully",
            content = @Content(schema = @Schema(implementation = VehiculeAdjustInfoResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Adjustment information with optional pictures",
            required = true,
            content = @Content(schema = @Schema(implementation = VehiculeAdjustInfoCreateRequest.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/adjust-infos")
    public ResponseEntity<?> createVehiculeAdjustInfo(
            @RequestBody VehiculeAdjustInfoCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            VehiculeAdjustInfoDTO adjustInfo = vehiculeService.createVehiculeAdjustInfo(request, user);
            return ResponseEntity.ok(new VehiculeAdjustInfoResponse(true, "Information d'ajustement créée avec succès", adjustInfo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get adjustment information pictures")
    @ApiResponse(
            responseCode = "200",
            description = "Pictures retrieved successfully",
            content = @Content(schema = @Schema(implementation = VehiculeAdjustInfoPictureListResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Adjustment information not found")
    @RequireRole("Utilisateur")
    @GetMapping("/adjust-infos/{adjustInfoId}/pictures")
    public ResponseEntity<?> getAdjustInfoPictures(@PathVariable UUID adjustInfoId) {
        try {
            List<VehiculeAdjustInfoPictureDTO> pictures = vehiculeService.getAdjustInfoPictures(adjustInfoId);
            return ResponseEntity.ok(new VehiculeAdjustInfoPictureListResponse(true, pictures));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
