package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.todo.TodoCreateRequest;
import bzh.stack.apiavtrans.dto.todo.TodoListResponse;
import bzh.stack.apiavtrans.dto.todo.TodoResponse;
import bzh.stack.apiavtrans.dto.todo.TodoSearchRequest;
import bzh.stack.apiavtrans.dto.todo.TodoUpdateRequest;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.TodoService;
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
@RequestMapping("/todos")
@RequiredArgsConstructor
@Tag(name = "Todos", description = "Shared todo management")
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "[UTILISATEUR] Create a new todo")
    @ApiResponse(
            responseCode = "200",
            description = "Todo created successfully",
            content = @Content(schema = @Schema(implementation = TodoResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping
    public ResponseEntity<?> createTodo(
            @Valid @RequestBody TodoCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            TodoResponse response = todoService.createTodo(user, request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Update a todo")
    @ApiResponse(
            responseCode = "200",
            description = "Todo updated successfully",
            content = @Content(schema = @Schema(implementation = TodoResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @RequireRole("Utilisateur")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateTodo(
            @Parameter(description = "Todo UUID") @PathVariable UUID uuid,
            @Valid @RequestBody TodoUpdateRequest request) {
        try {
            TodoResponse response = todoService.updateTodo(uuid, request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Toggle todo done status")
    @ApiResponse(
            responseCode = "200",
            description = "Todo status toggled successfully",
            content = @Content(schema = @Schema(implementation = TodoResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @RequireRole("Utilisateur")
    @PostMapping("/{uuid}/toggle")
    public ResponseEntity<?> toggleDone(
            @Parameter(description = "Todo UUID") @PathVariable UUID uuid,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            TodoResponse response = todoService.toggleDone(uuid, user);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Delete a todo")
    @ApiResponse(
            responseCode = "200",
            description = "Todo deleted successfully",
            content = @Content(schema = @Schema(implementation = TodoResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @RequireRole("Utilisateur")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteTodo(
            @Parameter(description = "Todo UUID") @PathVariable UUID uuid) {
        try {
            TodoResponse response = todoService.deleteTodo(uuid);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(false, response.getMessage()));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Get a todo by UUID")
    @ApiResponse(
            responseCode = "200",
            description = "Todo retrieved successfully",
            content = @Content(schema = @Schema(implementation = TodoResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @RequireRole("Utilisateur")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getTodoById(
            @Parameter(description = "Todo UUID") @PathVariable UUID uuid) {
        try {
            TodoResponse response = todoService.getTodoById(uuid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "[UTILISATEUR] Search todos with filters")
    @ApiResponse(
            responseCode = "200",
            description = "Todos retrieved successfully",
            content = @Content(schema = @Schema(implementation = TodoListResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/search")
    public ResponseEntity<?> searchTodos(
            @RequestBody(required = false) TodoSearchRequest request) {
        try {
            TodoListResponse response = todoService.searchTodos(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
