package bzh.stack.apiavtrans.dto.couchette;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a couchette")
public class CouchetteCreateRequest {
    @Schema(description = "Couchette date (optional, defaults to today)", example = "2025-01-15")
    private LocalDate date;
}
