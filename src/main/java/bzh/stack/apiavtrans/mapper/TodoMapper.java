package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.todo.TodoDTO;
import bzh.stack.apiavtrans.entity.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoMapper {

    private final TodoCategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public TodoDTO toDTO(Todo todo) {
        if (todo == null) {
            return null;
        }

        TodoDTO dto = new TodoDTO();
        dto.setUuid(todo.getUuid());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setCategory(categoryMapper.toDTO(todo.getCategory()));
        dto.setIsDone(todo.getIsDone());
        dto.setCompletedAt(todo.getCompletedAt());
        dto.setCompletedBy(userMapper.toDTO(todo.getCompletedBy()));
        dto.setCreatedBy(userMapper.toDTO(todo.getCreatedBy()));
        dto.setCreatedAt(todo.getCreatedAt());
        dto.setUpdatedAt(todo.getUpdatedAt());

        return dto;
    }
}
