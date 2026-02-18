package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.dto.stock.*;
import bzh.stack.apiavtrans.service.StockCategoryService;
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
@RequestMapping("/stock-categories")
@RequiredArgsConstructor
@Tag(name = "Stock Categories", description = "Gestion des catégories d'articles de stock")
public class StockCategoryController {

    private final StockCategoryService stockCategoryService;

    @Operation(
            summary = "[MÉCANICIEN] Récupérer toutes les catégories",
            description = "Retourne la liste de toutes les catégories de stock triées par nom"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des catégories récupérée avec succès",
            content = @Content(schema = @Schema(implementation = StockCategoryListResponse.class))
    )
    @RequireRole("Mécanicien")
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<StockCategoryDTO> categories = stockCategoryService.getAllCategories();
            return ResponseEntity.ok(new StockCategoryListResponse(true, categories));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Récupérer une catégorie par ID",
            description = "Retourne les détails d'une catégorie de stock spécifique"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catégorie récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = StockCategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(
            @Parameter(description = "UUID de la catégorie") @PathVariable UUID id) {
        try {
            StockCategoryDTO category = stockCategoryService.getCategoryById(id);
            return ResponseEntity.ok(new StockCategoryResponse(true, null, category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Créer une catégorie",
            description = "Crée une nouvelle catégorie de stock"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catégorie créée avec succès",
                    content = @Content(schema = @Schema(implementation = StockCategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou nom déjà existant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations de la catégorie à créer",
            required = true,
            content = @Content(schema = @Schema(implementation = StockCategoryCreateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody StockCategoryCreateRequest request) {
        try {
            StockCategoryDTO category = stockCategoryService.createCategory(request);
            return ResponseEntity.ok(new StockCategoryResponse(true, "Catégorie créée avec succès", category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Mettre à jour une catégorie",
            description = "Met à jour les informations d'une catégorie existante"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catégorie mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = StockCategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou nom déjà existant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations à mettre à jour",
            required = true,
            content = @Content(schema = @Schema(implementation = StockCategoryUpdateRequest.class))
    )
    @RequireRole("Mécanicien")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "UUID de la catégorie") @PathVariable UUID id,
            @Valid @RequestBody StockCategoryUpdateRequest request) {
        try {
            StockCategoryDTO category = stockCategoryService.updateCategory(id, request);
            return ResponseEntity.ok(new StockCategoryResponse(true, "Catégorie mise à jour avec succès", category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[MÉCANICIEN] Supprimer une catégorie",
            description = "Supprime définitivement une catégorie de stock"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catégorie supprimée avec succès",
                    content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @RequireRole("Mécanicien")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "UUID de la catégorie") @PathVariable UUID id) {
        try {
            stockCategoryService.deleteCategory(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Catégorie supprimée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
