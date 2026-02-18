package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeDTO;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeListResponse;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeResponse;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.RapportVehiculeService;
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

import java.util.UUID;

@RestController
@RequestMapping("/rapports")
@RequiredArgsConstructor
@Tag(name = "Vehicle Reports", description = "Vehicle report management with photos")
public class RapportVehiculeController {

    private final RapportVehiculeService rapportVehiculeService;

    @Operation(
            summary = "[MÉCANICIEN] Get all reports for a vehicle (paginated)",
            description = "Retrieves paginated reports for a vehicle. Use size=-1 to retrieve all records without pagination."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Reports retrieved successfully",
            content = @Content(schema = @Schema(implementation = RapportVehiculeListResponse.class))
    )
    @RequireRole("Mécanicien")
    @GetMapping("/{vehiculeId}")
    public ResponseEntity<?> getRapportsByVehiculeId(
            @Parameter(description = "Vehicle UUID") @PathVariable UUID vehiculeId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50). Use -1 to retrieve all records.", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        try {
            boolean fetchAll = (size == -1);
            if (!fetchAll && size > 50) {
                size = 50;
            }
            Pageable pageable = fetchAll ? Pageable.unpaged() : PageRequest.of(page, size);
            RapportVehiculeListResponse response = rapportVehiculeService.getRapportsByVehiculeId(vehiculeId, pageable, fetchAll);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Create a vehicle report")
    @ApiResponse(
            responseCode = "200",
            description = "Report created successfully",
            content = @Content(schema = @Schema(implementation = RapportVehiculeResponse.class))
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Report information to create",
            required = true,
            content = @Content(schema = @Schema(implementation = RapportVehiculeCreateRequest.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping
    public ResponseEntity<?> createRapport(
            @RequestBody RapportVehiculeCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            RapportVehiculeDTO rapport = rapportVehiculeService.createRapport(request, user);
            return ResponseEntity.ok(new RapportVehiculeResponse(true, "Rapport créé avec succès", rapport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get current user's latest report")
    @ApiResponse(
            responseCode = "200",
            description = "Latest report retrieved successfully",
            content = @Content(schema = @Schema(implementation = RapportVehiculeResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/me/latest")
    public ResponseEntity<?> getLatestUserRapport(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            RapportVehiculeDTO rapport = rapportVehiculeService.getLatestUserRapport(user);
            return ResponseEntity.ok(new RapportVehiculeResponse(true, "Dernier rapport récupéré avec succès", rapport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
