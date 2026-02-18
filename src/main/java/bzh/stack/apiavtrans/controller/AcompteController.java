package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.acompte.AcompteAdminCreateRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteCreateRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteListResponse;
import bzh.stack.apiavtrans.dto.acompte.AcompteResponse;
import bzh.stack.apiavtrans.dto.acompte.AcompteSearchRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteUpdateRequest;
import bzh.stack.apiavtrans.dto.acompte.AcompteValidationRequest;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.AcompteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/acomptes")
@RequiredArgsConstructor
@Tag(name = "Acomptes", description = "Salary advance requests management")
public class AcompteController {

    private final AcompteService acompteService;

    @Operation(summary = "[UTILISATEUR] Create an advance request")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request created successfully",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping
    public ResponseEntity<?> createAcompte(
            @Valid @RequestBody AcompteCreateRequest request,
            HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");
        AcompteResponse response = acompteService.createAcompte(user.getEmail(), request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[UTILISATEUR] Get my advance requests")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of user's advance requests retrieved successfully (defaults to last 30 days)",
            content = @Content(schema = @Schema(implementation = AcompteListResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/my")
    public ResponseEntity<?> getMyAcomptes(
            @RequestBody(required = false) AcompteSearchRequest request,
            HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");
        if (request == null) {
            request = new AcompteSearchRequest();
        }
        AcompteListResponse response = acompteService.getMyAcomptes(user.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[UTILISATEUR] Cancel an advance request")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request cancelled successfully (only allowed for pending requests)",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Utilisateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> cancelAcompte(
            @Parameter(description = "Advance request UUID") @PathVariable UUID uuid,
            HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");
        AcompteResponse response = acompteService.cancelAcompte(uuid, user.getEmail());

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[ADMINISTRATEUR] Create an advance request for a user (auto-approved)")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request created and approved successfully",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/new")
    public ResponseEntity<?> createAcompteAsAdmin(
            @Valid @RequestBody AcompteAdminCreateRequest request,
            HttpServletRequest httpRequest) {
        User admin = (User) httpRequest.getAttribute("user");
        AcompteResponse response = acompteService.createAcompteAsAdmin(request, admin);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[ADMINISTRATEUR] Search advance requests")
    @ApiResponse(
            responseCode = "200",
            description = "Filtered advance requests retrieved successfully (defaults to last 30 days)",
            content = @Content(schema = @Schema(implementation = AcompteListResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/search")
    public ResponseEntity<?> searchAcomptes(
            @RequestBody(required = false) AcompteSearchRequest request) {
        if (request == null) {
            request = new AcompteSearchRequest();
        }
        AcompteListResponse response = acompteService.getAllAcomptes(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[ADMINISTRATEUR] Approve or reject an advance request")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request validated or rejected successfully",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/{uuid}/validate")
    public ResponseEntity<?> validateAcompte(
            @Parameter(description = "Advance request UUID") @PathVariable UUID uuid,
            @Valid @RequestBody AcompteValidationRequest request,
            HttpServletRequest httpRequest) {
        User admin = (User) httpRequest.getAttribute("user");
        AcompteResponse response = acompteService.validateAcompte(uuid, admin.getEmail(), request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[ADMINISTRATEUR] Update an advance request")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request updated successfully",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Administrateur")
    @PutMapping("/admin/{uuid}")
    public ResponseEntity<?> updateAcompte(
            @Parameter(description = "Advance request UUID") @PathVariable UUID uuid,
            @Valid @RequestBody AcompteUpdateRequest request,
            HttpServletRequest httpRequest) {
        User admin = (User) httpRequest.getAttribute("user");
        AcompteResponse response = acompteService.updateAcompte(uuid, admin.getEmail(), request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[ADMINISTRATEUR] Delete an advance request")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request permanently deleted",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Administrateur")
    @DeleteMapping("/admin/{uuid}")
    public ResponseEntity<?> deleteAcompte(
            @Parameter(description = "Advance request UUID") @PathVariable UUID uuid) {
        AcompteResponse response = acompteService.deleteAcompte(uuid);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[ADMINISTRATEUR] Get advance request by UUID")
    @ApiResponse(
            responseCode = "200",
            description = "Advance request details retrieved successfully",
            content = @Content(schema = @Schema(implementation = AcompteResponse.class))
    )
    @RequireRole("Administrateur")
    @GetMapping("/admin/{uuid}")
    public ResponseEntity<?> getAcompteById(
            @Parameter(description = "Advance request UUID") @PathVariable UUID uuid) {
        AcompteResponse response = acompteService.getAcompteById(uuid);
        return ResponseEntity.ok(response);
    }
}
