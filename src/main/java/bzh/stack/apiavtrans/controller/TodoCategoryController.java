package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.todo.TodoCategoryCreateRequest;
import bzh.stack.apiavtrans.dto.todo.TodoCategoryListResponse;
import bzh.stack.apiavtrans.dto.todo.TodoCategoryResponse;
import bzh.stack.apiavtrans.service.TodoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/todo-categories")
@RequiredArgsConstructor
@Tag(name = "Todo Categories", description = "Todo category management")
public class TodoCategoryController {

    private final TodoCategoryService categoryService;

    @Operation(summary = "[UTILISATEUR] Get all todo categories")
    @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(schema = @Schema(implementation = TodoCategoryListResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            TodoCategoryListResponse response = categoryService.getAllCategories();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Create a new todo category")
    @ApiResponse(
            responseCode = "200",
            description = "Category created successfully",
            content = @Content(schema = @Schema(implementation = TodoCategoryResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody TodoCategoryCreateRequest request) {
        try {
            TodoCategoryResponse response = categoryService.createCategory(request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Update a todo category")
    @ApiResponse(
            responseCode = "200",
            description = "Category updated successfully",
            content = @Content(schema = @Schema(implementation = TodoCategoryResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Category not found")
    @RequireRole("Utilisateur")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "Category UUID") @PathVariable UUID uuid,
            @Valid @RequestBody TodoCategoryCreateRequest request) {
        try {
            TodoCategoryResponse response = categoryService.updateCategory(uuid, request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Delete a todo category")
    @ApiResponse(
            responseCode = "200",
            description = "Category deleted successfully",
            content = @Content(schema = @Schema(implementation = TodoCategoryResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Category not found")
    @RequireRole("Utilisateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "Category UUID") @PathVariable UUID uuid) {
        try {
            TodoCategoryResponse response = categoryService.deleteCategory(uuid);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get a todo category by UUID")
    @ApiResponse(
            responseCode = "200",
            description = "Category retrieved successfully",
            content = @Content(schema = @Schema(implementation = TodoCategoryResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Category not found")
    @RequireRole("Utilisateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getCategoryById(
            @Parameter(description = "Category UUID") @PathVariable UUID uuid) {
        try {
            TodoCategoryResponse response = categoryService.getCategoryById(uuid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
