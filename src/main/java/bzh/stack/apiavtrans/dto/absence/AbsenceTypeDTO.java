package bzh.stack.apiavtrans.dto.absence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceTypeDTO {
    private UUID uuid;
    private String name;
    private String color;
    private ZonedDateTime createdAt;
}
