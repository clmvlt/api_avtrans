package bzh.stack.apiavtrans.dto.acompte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteListResponse {
    private boolean success;
    private List<AcompteDTO> acomptes;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
