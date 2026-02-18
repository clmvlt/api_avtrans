package bzh.stack.apiavtrans.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {
    private boolean success;
    private String message;
    private TodoDTO todo;
}
