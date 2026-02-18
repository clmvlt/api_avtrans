package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.couchette.*;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.entity.Couchette;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.CouchetteMapper;
import bzh.stack.apiavtrans.service.CouchetteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/couchettes")
@RequiredArgsConstructor
@Tag(name = "Couchettes", description = "Couchette management for users with couchette permission")
public class CouchetteController {

    private final CouchetteService couchetteService;
    private final CouchetteMapper couchetteMapper;

    @Operation(
            summary = "[UTILISATEUR] Create a couchette",
            description = "Creates a couchette for the current day. User must have isCouchette permission. Max one per day."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Couchette created successfully",
            content = @Content(schema = @Schema(implementation = CouchetteDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "User doesn't have couchette permission or couchette already exists for this date")
    @RequireRole("Utilisateur")
    @PostMapping
    public ResponseEntity<?> createCouchette(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Couchette creation data (date optional, defaults to today)",
                    content = @Content(schema = @Schema(implementation = CouchetteCreateRequest.class))
            )
            @RequestBody(required = false) CouchetteCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            Couchette couchette = couchetteService.createCouchette(
                    user,
                    request != null ? request.getDate() : null
            );
            CouchetteDTO dto = couchetteMapper.toDTO(couchette);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get my couchettes",
            description = "Retrieves paginated list of couchettes for the authenticated user, ordered by most recent."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Couchettes retrieved successfully",
            content = @Content(schema = @Schema(implementation = CouchettePagedResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/me")
    public ResponseEntity<?> getMyCouchettes(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            Page<Couchette> couchettePage = couchetteService.getCouchettesForUser(user, page, size);

            List<CouchetteDTO> couchetteDTOs = couchettePage.getContent().stream()
                    .map(couchetteMapper::toDTO)
                    .collect(Collectors.toList());

            CouchettePagedResponse response = new CouchettePagedResponse(
                    true,
                    couchetteDTOs,
                    couchettePage.getNumber(),
                    couchettePage.getSize(),
                    couchettePage.getTotalElements(),
                    couchettePage.getTotalPages(),
                    couchettePage.isFirst(),
                    couchettePage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Delete my couchette",
            description = "Deletes a couchette. Users can only delete their own couchettes from the current day."
    )
    @ApiResponse(responseCode = "204", description = "Couchette deleted successfully")
    @ApiResponse(responseCode = "400", description = "Cannot delete couchette (not yours or not from today)")
    @ApiResponse(responseCode = "404", description = "Couchette not found")
    @RequireRole("Utilisateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteMyCouchette(
            @Parameter(description = "Couchette UUID") @PathVariable UUID uuid,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            couchetteService.deleteCouchette(uuid, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Operation(
            summary = "[ADMINISTRATEUR] Search and filter couchettes",
            description = "Retrieves paginated list of couchettes with optional filters (user, date range)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Couchettes retrieved successfully",
            content = @Content(schema = @Schema(implementation = CouchettePagedResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/search")
    public ResponseEntity<?> searchCouchettes(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Search filters and pagination",
                    content = @Content(schema = @Schema(implementation = CouchetteSearchRequest.class))
            )
            @RequestBody(required = false) CouchetteSearchRequest request) {
        try {
            if (request == null) {
                request = new CouchetteSearchRequest();
            }
            Page<Couchette> couchettePage = couchetteService.searchCouchettes(request);

            List<CouchetteDTO> couchetteDTOs = couchettePage.getContent().stream()
                    .map(couchetteMapper::toDTO)
                    .collect(Collectors.toList());

            CouchettePagedResponse response = new CouchettePagedResponse(
                    true,
                    couchetteDTOs,
                    couchettePage.getNumber(),
                    couchettePage.getSize(),
                    couchettePage.getTotalElements(),
                    couchettePage.getTotalPages(),
                    couchettePage.isFirst(),
                    couchettePage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get couchettes for a user",
            description = "Retrieves paginated list of couchettes for a specific user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Couchettes retrieved successfully",
            content = @Content(schema = @Schema(implementation = CouchettePagedResponse.class))
    )
    @RequireRole("Administrateur")
    @GetMapping("/admin/user/{userUuid}")
    public ResponseEntity<?> getCouchettesForUser(
            @Parameter(description = "User UUID") @PathVariable UUID userUuid,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Couchette> couchettePage = couchetteService.getCouchettesForUserByUuid(userUuid, page, size);

            List<CouchetteDTO> couchetteDTOs = couchettePage.getContent().stream()
                    .map(couchetteMapper::toDTO)
                    .collect(Collectors.toList());

            CouchettePagedResponse response = new CouchettePagedResponse(
                    true,
                    couchetteDTOs,
                    couchettePage.getNumber(),
                    couchettePage.getSize(),
                    couchettePage.getTotalElements(),
                    couchettePage.getTotalPages(),
                    couchettePage.isFirst(),
                    couchettePage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Create couchette for a user",
            description = "Creates a couchette for a specific user on a specific date."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Couchette created successfully",
            content = @Content(schema = @Schema(implementation = CouchetteDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Couchette already exists for this user on this date")
    @RequireRole("Administrateur")
    @PostMapping("/admin")
    public ResponseEntity<?> createCouchetteForUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Couchette creation data",
                    content = @Content(schema = @Schema(implementation = AdminCouchetteCreateRequest.class))
            )
            @RequestBody AdminCouchetteCreateRequest request) {
        try {
            Couchette couchette = couchetteService.createCouchetteByAdmin(
                    request.getUserUuid(),
                    request.getDate()
            );
            CouchetteDTO dto = couchetteMapper.toDTO(couchette);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Delete any couchette",
            description = "Deletes any couchette regardless of date or owner."
    )
    @ApiResponse(responseCode = "204", description = "Couchette deleted successfully")
    @ApiResponse(responseCode = "404", description = "Couchette not found")
    @RequireRole("Administrateur")
    @DeleteMapping("/admin/{uuid}")
    public ResponseEntity<?> deleteCouchetteByAdmin(
            @Parameter(description = "Couchette UUID") @PathVariable UUID uuid) {
        try {
            couchetteService.deleteCouchetteByAdmin(uuid);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
