package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.todo.TodoCategoryCreateRequest;
import bzh.stack.apiavtrans.dto.todo.TodoCategoryDTO;
import bzh.stack.apiavtrans.dto.todo.TodoCategoryListResponse;
import bzh.stack.apiavtrans.dto.todo.TodoCategoryResponse;
import bzh.stack.apiavtrans.entity.Todo;
import bzh.stack.apiavtrans.entity.TodoCategory;
import bzh.stack.apiavtrans.mapper.TodoCategoryMapper;
import bzh.stack.apiavtrans.repository.TodoCategoryRepository;
import bzh.stack.apiavtrans.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoCategoryService {

    private final TodoCategoryRepository categoryRepository;
    private final TodoCategoryMapper categoryMapper;
    private final TodoRepository todoRepository;

    public TodoCategoryListResponse getAllCategories() {
        List<TodoCategory> categories = categoryRepository.findAll();
        List<TodoCategoryDTO> categoryDTOs = categories.stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());

        return new TodoCategoryListResponse(true, categoryDTOs);
    }

    @Transactional
    public TodoCategoryResponse createCategory(TodoCategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            return new TodoCategoryResponse(false, "A category with this name already exists", null);
        }

        TodoCategory category = new TodoCategory();
        category.setName(request.getName());
        category.setColor(request.getColor());

        TodoCategory saved = categoryRepository.save(category);

        return new TodoCategoryResponse(true, "Category created successfully", categoryMapper.toDTO(saved));
    }

    @Transactional
    public TodoCategoryResponse updateCategory(UUID categoryUuid, TodoCategoryCreateRequest request) {
        TodoCategory category = categoryRepository.findById(categoryUuid)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            return new TodoCategoryResponse(false, "A category with this name already exists", null);
        }

        category.setName(request.getName());
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        TodoCategory saved = categoryRepository.save(category);

        return new TodoCategoryResponse(true, "Category updated successfully", categoryMapper.toDTO(saved));
    }

    @Transactional
    public TodoCategoryResponse deleteCategory(UUID categoryUuid) {
        TodoCategory category = categoryRepository.findById(categoryUuid)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Todo> todosWithCategory = todoRepository.findByCategory(category);
        for (Todo todo : todosWithCategory) {
            todo.setCategory(null);
            todoRepository.save(todo);
        }

        categoryRepository.delete(category);

        return new TodoCategoryResponse(true, "Category deleted successfully", null);
    }

    public TodoCategoryResponse getCategoryById(UUID categoryUuid) {
        TodoCategory category = categoryRepository.findById(categoryUuid)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return new TodoCategoryResponse(true, null, categoryMapper.toDTO(category));
    }
}
