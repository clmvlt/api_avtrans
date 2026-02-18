package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.signature.*;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/signatures")
@RequiredArgsConstructor
@Tag(name = "Signatures", description = "Work hours signature management")
public class SignatureController {

    private final SignatureService signatureService;

    @Operation(summary = "[UTILISATEUR] Create a signature")
    @ApiResponse(
            responseCode = "200",
            description = "Signature created successfully",
            content = @Content(schema = @Schema(implementation = SignatureResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping
    public ResponseEntity<?> createSignature(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Signature data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SignatureCreateRequest.class))
            )
            @RequestBody SignatureCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            SignatureResponse response = signatureService.createSignature(authenticatedUser.getEmail(), request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get all my signatures")
    @ApiResponse(
            responseCode = "200",
            description = "Signatures retrieved successfully",
            content = @Content(schema = @Schema(implementation = SignatureListResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping
    public ResponseEntity<?> getMySignatures(HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            SignatureListResponse response = signatureService.getUserSignatures(authenticatedUser.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get my last signature with Base64 image")
    @ApiResponse(
            responseCode = "200",
            description = "Last signature retrieved successfully",
            content = @Content(schema = @Schema(implementation = SignatureResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/last")
    public ResponseEntity<?> getMyLastSignature(HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            SignatureResponse response = signatureService.getLastSignature(authenticatedUser.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get last signature summary without Base64")
    @ApiResponse(
            responseCode = "200",
            description = "Last signature summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = LastSignatureSummaryDTO.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/last/summary")
    public ResponseEntity<?> getLastSignatureSummary(HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            LastSignatureSummaryDTO response = signatureService.getLastSignatureSummary(authenticatedUser.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get all signatures for a specific user")
    @ApiResponse(
            responseCode = "200",
            description = "User signatures retrieved successfully",
            content = @Content(schema = @Schema(implementation = SignatureListResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "User not found")
    @RequireRole("Administrateur")
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> getUserSignatures(
            @io.swagger.v3.oas.annotations.Parameter(description = "User UUID") @PathVariable UUID userUuid) {
        try {
            SignatureListResponse response = signatureService.getSignaturesByUserUuid(userUuid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get all users with their last signature")
    @ApiResponse(
            responseCode = "200",
            description = "All users with last signature retrieved successfully",
            content = @Content(schema = @Schema(implementation = UsersWithLastSignatureListResponse.class))
    )
    @RequireRole("Administrateur")
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsersWithLastSignature() {
        try {
            UsersWithLastSignatureListResponse response = signatureService.getAllUsersWithLastSignature();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Delete a signature")
    @ApiResponse(
            responseCode = "200",
            description = "Signature deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Signature not found")
    @RequireRole("Administrateur")
    @DeleteMapping("/{signatureUuid}")
    public ResponseEntity<?> deleteSignature(
            @io.swagger.v3.oas.annotations.Parameter(description = "Signature UUID") @PathVariable UUID signatureUuid) {
        try {
            signatureService.deleteSignature(signatureUuid);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Signature supprimée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
