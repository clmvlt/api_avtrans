package bzh.stack.apiavtrans.dto.vehicule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant une photo d'information d'ajustement")
public class VehiculeAdjustInfoPictureDTO {

    @Schema(description = "Identifiant unique de la photo", example = "def5678-e89b-12d3-a456-426614174555")
    private UUID id;

    @Schema(description = "Identifiant de l'information d'ajustement", example = "abc1234-e89b-12d3-a456-426614174444")
    private UUID adjustInfoId;

    @Schema(description = "URL de l'image", example = "http://192.168.1.120:8081/uploads/vehicules/adjust/def5678_1234567890.jpg")
    private String pictureUrl;

    @Schema(description = "Date de création de la photo", example = "2025-01-15T16:01:00+01:00")
    private ZonedDateTime createdAt;
}
