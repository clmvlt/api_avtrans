package bzh.stack.apiavtrans.dto.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCreateRequest {
    private UUID userUuid;
    private ZonedDateTime debut;
    private ZonedDateTime fin;
    private Double latitude;
    private Double longitude;
    private Boolean isBreak;
}
