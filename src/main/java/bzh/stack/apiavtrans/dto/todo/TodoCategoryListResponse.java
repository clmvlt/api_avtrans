package bzh.stack.apiavtrans.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryListResponse {
    private boolean success;
    private List<TodoCategoryDTO> categories;
}
