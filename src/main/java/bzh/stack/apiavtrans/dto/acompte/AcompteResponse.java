package bzh.stack.apiavtrans.dto.acompte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompteResponse {
    private boolean success;
    private String message;
    private AcompteDTO acompte;
}
