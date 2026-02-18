package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.dto.todo.TodoCreateRequest;
import bzh.stack.apiavtrans.dto.todo.TodoDTO;
import bzh.stack.apiavtrans.dto.todo.TodoListResponse;
import bzh.stack.apiavtrans.dto.todo.TodoResponse;
import bzh.stack.apiavtrans.dto.todo.TodoSearchRequest;
import bzh.stack.apiavtrans.dto.todo.TodoUpdateRequest;
import bzh.stack.apiavtrans.entity.Todo;
import bzh.stack.apiavtrans.entity.TodoCategory;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.TodoMapper;
import bzh.stack.apiavtrans.repository.TodoCategoryRepository;
import bzh.stack.apiavtrans.repository.TodoRepository;
import bzh.stack.apiavtrans.specification.TodoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoCategoryRepository categoryRepository;
    private final TodoMapper todoMapper;
    private final NotificationService notificationService;

    @Transactional
    public TodoResponse createTodo(User creator, TodoCreateRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCreatedBy(creator);
        todo.setIsDone(false);

        if (request.getCategoryUuid() != null) {
            TodoCategory category = categoryRepository.findById(request.getCategoryUuid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            todo.setCategory(category);
        }

        Todo saved = todoRepository.save(todo);

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle("Nouvelle tâche créée");
        notificationRequest.setDescription(String.format("%s %s a créé la tâche : %s",
            creator.getFirstName(), creator.getLastName(), saved.getTitle()));
        notificationRequest.setRefType("todo");
        notificationRequest.setRefId(saved.getUuid().toString());

        notificationService.sendNotificationToRoleWithPreferenceExcluding("Administrateur", notificationRequest, "todo", creator.getUuid());

        return new TodoResponse(true, "Todo created successfully", todoMapper.toDTO(saved));
    }

    @Transactional
    public TodoResponse updateTodo(UUID todoUuid, TodoUpdateRequest request) {
        Todo todo = todoRepository.findById(todoUuid)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getCategoryUuid() != null) {
            TodoCategory category = categoryRepository.findById(request.getCategoryUuid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            todo.setCategory(category);
        }

        Todo saved = todoRepository.save(todo);

        return new TodoResponse(true, "Todo updated successfully", todoMapper.toDTO(saved));
    }

    @Transactional
    public TodoResponse toggleDone(UUID todoUuid, User user) {
        Todo todo = todoRepository.findById(todoUuid)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        boolean newDoneStatus = !todo.getIsDone();
        todo.setIsDone(newDoneStatus);

        if (newDoneStatus) {
            todo.setCompletedAt(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
            todo.setCompletedBy(user);
        } else {
            todo.setCompletedAt(null);
            todo.setCompletedBy(null);
        }

        Todo saved = todoRepository.save(todo);

        String message = newDoneStatus ? "Todo marked as done" : "Todo marked as not done";
        return new TodoResponse(true, message, todoMapper.toDTO(saved));
    }

    @Transactional
    public TodoResponse deleteTodo(UUID todoUuid) {
        Todo todo = todoRepository.findById(todoUuid)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        todoRepository.delete(todo);

        return new TodoResponse(true, "Todo deleted successfully", null);
    }

    public TodoResponse getTodoById(UUID todoUuid) {
        Todo todo = todoRepository.findById(todoUuid)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        return new TodoResponse(true, null, todoMapper.toDTO(todo));
    }

    public TodoListResponse searchTodos(TodoSearchRequest request) {
        if (request == null) {
            request = new TodoSearchRequest();
        }

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDir = request.getSortDirection() != null ? request.getSortDirection() : "DESC";

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Todo> spec = TodoSpecifications.buildSearchSpec(
                request.getCategoryUuid(),
                request.getIsDone(),
                request.getStartDate(),
                request.getEndDate()
        );

        Page<Todo> todoPage = todoRepository.findAll(spec, pageable);

        List<TodoDTO> todoDTOs = todoPage.getContent().stream()
                .map(todoMapper::toDTO)
                .collect(Collectors.toList());

        return new TodoListResponse(
                true,
                todoDTOs,
                todoPage.getTotalPages(),
                todoPage.getTotalElements(),
                todoPage.getNumber()
        );
    }
}
