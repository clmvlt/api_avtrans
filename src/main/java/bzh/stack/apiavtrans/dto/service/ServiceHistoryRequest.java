package bzh.stack.apiavtrans.dto.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHistoryRequest {
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isBreak;
}
