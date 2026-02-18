package bzh.stack.apiavtrans.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoSearchRequest {
    private UUID categoryUuid;
    private Boolean isDone;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
