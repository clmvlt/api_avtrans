package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Requête pour terminer un service")
public class ServiceEndRequest {
    @Schema(description = "Latitude GPS", example = "48.8566")
    private Double latitude;

    @Schema(description = "Longitude GPS", example = "2.3522")
    private Double longitude;

    @Schema(description = "UUID de l'utilisateur (réservé aux administrateurs). Si non fourni, utilise l'utilisateur authentifié",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userUuid;
}
