package bzh.stack.apiavtrans.dto.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private UUID uuid;
    private ZonedDateTime debut;
    private ZonedDateTime fin;
    private Long duree;
    private Boolean isBreak;
    private Double latitude;
    private Double longitude;
    private Boolean isAdmin;
    private UUID userUuid;
}
