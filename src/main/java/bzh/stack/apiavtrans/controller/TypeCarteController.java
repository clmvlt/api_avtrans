package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.carte.TypeCarteCreateRequest;
import bzh.stack.apiavtrans.dto.carte.TypeCarteDTO;
import bzh.stack.apiavtrans.dto.carte.TypeCarteUpdateRequest;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.entity.TypeCarte;
import bzh.stack.apiavtrans.mapper.TypeCarteMapper;
import bzh.stack.apiavtrans.service.TypeCarteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/type-cartes")
@RequiredArgsConstructor
@Tag(name = "Card Types", description = "Card type management operations (Admin only)")
public class TypeCarteController {

    private final TypeCarteService typeCarteService;
    private final TypeCarteMapper typeCarteMapper;

    @Operation(summary = "[ADMINISTRATEUR] Get all card types")
    @ApiResponse(
            responseCode = "200",
            description = "Card types retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TypeCarteDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping
    public ResponseEntity<?> getAllTypeCartes() {
        try {
            List<TypeCarteDTO> typeCartes = typeCarteService.getAllTypeCartes()
                    .stream()
                    .map(typeCarteMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(typeCartes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get card type by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card type found",
                    content = @Content(schema = @Schema(implementation = TypeCarteDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card type not found")
    })
    @RequireRole("Administrateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getTypeCarte(
            @Parameter(description = "Card type UUID") @PathVariable UUID uuid) {
        try {
            TypeCarte typeCarte = typeCarteService.getTypeCarte(uuid)
                    .orElseThrow(() -> new RuntimeException("Type de carte non trouve"));
            return ResponseEntity.ok(typeCarteMapper.toDTO(typeCarte));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Create a new card type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card type created successfully",
                    content = @Content(schema = @Schema(implementation = TypeCarteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @RequireRole("Administrateur")
    @PostMapping
    public ResponseEntity<?> createTypeCarte(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card type data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TypeCarteCreateRequest.class))
            )
            @Valid @RequestBody TypeCarteCreateRequest request) {
        try {
            TypeCarte typeCarte = typeCarteService.createTypeCarte(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Type de carte cree avec succes");
            response.put("typeCarte", typeCarteMapper.toDTO(typeCarte));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Update a card type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card type updated successfully",
                    content = @Content(schema = @Schema(implementation = TypeCarteDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card type not found")
    })
    @RequireRole("Administrateur")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateTypeCarte(
            @Parameter(description = "Card type UUID") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card type data to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TypeCarteUpdateRequest.class))
            )
            @Valid @RequestBody TypeCarteUpdateRequest request) {
        try {
            TypeCarte typeCarte = typeCarteService.updateTypeCarte(uuid, request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Type de carte mis a jour avec succes");
            response.put("typeCarte", typeCarteMapper.toDTO(typeCarte));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Delete a card type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card type not found")
    })
    @RequireRole("Administrateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteTypeCarte(
            @Parameter(description = "Card type UUID") @PathVariable UUID uuid) {
        try {
            typeCarteService.deleteTypeCarte(uuid);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Type de carte supprime avec succes");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
