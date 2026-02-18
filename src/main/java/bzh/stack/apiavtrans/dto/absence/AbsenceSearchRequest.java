package bzh.stack.apiavtrans.dto.absence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceSearchRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private UUID absenceTypeUuid;
    private UUID userUuid;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
    private Boolean includePast;
}
