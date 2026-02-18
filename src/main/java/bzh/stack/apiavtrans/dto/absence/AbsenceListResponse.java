package bzh.stack.apiavtrans.dto.absence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceListResponse {
    private boolean success;
    private List<AbsenceDTO> absences;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
