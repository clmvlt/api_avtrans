package bzh.stack.apiavtrans.dto.acompte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteSearchRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double montantMin;
    private Double montantMax;
    private UUID userUuid;
    private Boolean isPaid;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
