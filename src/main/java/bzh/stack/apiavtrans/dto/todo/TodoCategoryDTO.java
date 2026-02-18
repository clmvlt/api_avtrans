package bzh.stack.apiavtrans.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryDTO {
    private UUID uuid;
    private String name;
    private String color;
    private ZonedDateTime createdAt;
}
