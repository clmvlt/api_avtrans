package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.carte.CarteCreateRequest;
import bzh.stack.apiavtrans.dto.carte.CarteDTO;
import bzh.stack.apiavtrans.dto.carte.CarteResponse;
import bzh.stack.apiavtrans.dto.carte.CarteUpdateRequest;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.entity.Carte;
import bzh.stack.apiavtrans.mapper.CarteMapper;
import bzh.stack.apiavtrans.service.CarteService;
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
@RequestMapping("/cartes")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Card management operations (Admin only)")
public class CarteController {

    private final CarteService carteService;
    private final CarteMapper carteMapper;

    @Operation(summary = "[ADMINISTRATEUR] Get all cards")
    @ApiResponse(
            responseCode = "200",
            description = "Cards retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CarteDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping
    public ResponseEntity<?> getAllCartes() {
        try {
            List<CarteDTO> cartes = carteService.getAllCartes()
                    .stream()
                    .map(carteMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cartes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get card by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found",
                    content = @Content(schema = @Schema(implementation = CarteDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @RequireRole("Administrateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getCarte(
            @Parameter(description = "Card UUID") @PathVariable UUID uuid) {
        try {
            Carte carte = carteService.getCarte(uuid)
                    .orElseThrow(() -> new RuntimeException("Carte non trouvee"));
            return ResponseEntity.ok(carteMapper.toDTO(carte));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get cards by user")
    @ApiResponse(
            responseCode = "200",
            description = "Cards retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CarteDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> getCartesByUser(
            @Parameter(description = "User UUID") @PathVariable UUID userUuid) {
        try {
            List<CarteDTO> cartes = carteService.getCartesByUser(userUuid)
                    .stream()
                    .map(carteMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cartes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Get cards by type")
    @ApiResponse(
            responseCode = "200",
            description = "Cards retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CarteDTO.class))
            )
    )
    @RequireRole("Administrateur")
    @GetMapping("/type/{typeCarteUuid}")
    public ResponseEntity<?> getCartesByType(
            @Parameter(description = "Card type UUID") @PathVariable UUID typeCarteUuid) {
        try {
            List<CarteDTO> cartes = carteService.getCartesByTypeCarte(typeCarteUuid)
                    .stream()
                    .map(carteMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cartes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Create a new card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card created successfully",
                    content = @Content(schema = @Schema(implementation = CarteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @RequireRole("Administrateur")
    @PostMapping
    public ResponseEntity<?> createCarte(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CarteCreateRequest.class))
            )
            @Valid @RequestBody CarteCreateRequest request) {
        try {
            Carte carte = carteService.createCarte(request);
            return ResponseEntity.ok(new CarteResponse(true, "Carte creee avec succes", carteMapper.toDTO(carte)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Update a card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully",
                    content = @Content(schema = @Schema(implementation = CarteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @RequireRole("Administrateur")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateCarte(
            @Parameter(description = "Card UUID") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card data to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CarteUpdateRequest.class))
            )
            @Valid @RequestBody CarteUpdateRequest request) {
        try {
            Carte carte = carteService.updateCarte(uuid, request);
            return ResponseEntity.ok(new CarteResponse(true, "Carte mise a jour avec succes", carteMapper.toDTO(carte)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[ADMINISTRATEUR] Delete a card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @RequireRole("Administrateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteCarte(
            @Parameter(description = "Card UUID") @PathVariable UUID uuid) {
        try {
            carteService.deleteCarte(uuid);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Carte supprimee avec succes");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
