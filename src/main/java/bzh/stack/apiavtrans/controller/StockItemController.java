package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.stock.*;
import bzh.stack.apiavtrans.service.StockItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock-items")
@RequiredArgsConstructor
@Tag(name = "Stock Items", description = "Gestion des articles de stock (pièces, fournitures, etc.)")
public class StockItemController {

    private final StockItemService stockItemService;

    @Operation(
            summary = "[MÉCANICIEN] Récupérer tous les articles de stock",
            description = "Retourne la liste de tous les articles de stock triés par nom"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des articles récupérée avec succès",
            content = @Content(schema = @Schema(implementation = StockItemListResponse.class))
    )
    @RequireRole("Mécanicien")
    @GetMapping
    public ResponseEntity<?> getAllStockItems() {
        try {
            List<StockItemDTO> stockItems = stockItemService.getAllStockItems();
            return ResponseEntity.ok(new StockItemListResponse(true, stockItems));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Récupérer un article par ID",
            description = "Retourne les détails d'un article de stock spécifique"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Article récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = StockItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStockItemById(
            @Parameter(description = "UUID de l'article") @PathVariable UUID id) {
        try {
            StockItemDTO stockItem = stockItemService.getStockItemById(id);
            return ResponseEntity.ok(new StockItemResponse(true, null, stockItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Créer un article de stock",
            description = "Crée un nouvel article de stock avec les informations fournies"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Article créé avec succès",
                    content = @Content(schema = @Schema(implementation = StockItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou référence déjà existante",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations de l'article à créer",
            required = true,
            content = @Content(schema = @Schema(implementation = StockItemCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createStockItem(@Valid @RequestBody StockItemCreateRequest request) {
        try {
            StockItemDTO stockItem = stockItemService.createStockItem(request);
            return ResponseEntity.ok(new StockItemResponse(true, "Article créé avec succès", stockItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Mettre à jour un article de stock",
            description = "Met à jour les informations d'un article existant. Seuls les champs fournis seront mis à jour."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Article mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = StockItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou référence déjà existante",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations à mettre à jour",
            required = true,
            content = @Content(schema = @Schema(implementation = StockItemUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStockItem(
            @Parameter(description = "UUID de l'article") @PathVariable UUID id,
            @Valid @RequestBody StockItemUpdateRequest request) {
        try {
            StockItemDTO stockItem = stockItemService.updateStockItem(id, request);
            return ResponseEntity.ok(new StockItemResponse(true, "Article mis à jour avec succès", stockItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Supprimer un article de stock",
            description = "Supprime définitivement un article de stock"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Article supprimé avec succès",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStockItem(
            @Parameter(description = "UUID de l'article") @PathVariable UUID id) {
        try {
            stockItemService.deleteStockItem(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Article supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
