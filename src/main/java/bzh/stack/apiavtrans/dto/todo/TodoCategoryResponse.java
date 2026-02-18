package bzh.stack.apiavtrans.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryResponse {
    private boolean success;
    private String message;
    private TodoCategoryDTO category;
}
