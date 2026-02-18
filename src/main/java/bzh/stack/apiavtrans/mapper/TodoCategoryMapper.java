package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.todo.TodoCategoryDTO;
import bzh.stack.apiavtrans.entity.TodoCategory;
import org.springframework.stereotype.Component;

@Component
public class TodoCategoryMapper {

    public TodoCategoryDTO toDTO(TodoCategory category) {
        if (category == null) {
            return null;
        }

        TodoCategoryDTO dto = new TodoCategoryDTO();
        dto.setUuid(category.getUuid());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        dto.setCreatedAt(category.getCreatedAt());

        return dto;
    }
}
