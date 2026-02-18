package bzh.stack.apiavtrans.dto.todo;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoDTO {
    private UUID uuid;
    private String title;
    private String description;
    private TodoCategoryDTO category;
    private Boolean isDone;
    private ZonedDateTime completedAt;
    private UserDTO completedBy;
    private UserDTO createdBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
