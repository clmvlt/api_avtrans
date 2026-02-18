package bzh.stack.apiavtrans.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoListResponse {
    private boolean success;
    private List<TodoDTO> todos;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
