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
@Schema(description = "DTO représentant une photo de rapport de véhicule")
public class RapportVehiculePictureDTO {

    @Schema(description = "Identifiant unique de la photo", example = "890e1234-e89b-12d3-a456-426614174333")
    private UUID id;

    @Schema(description = "Identifiant du rapport associé", example = "789e0123-e89b-12d3-a456-426614174222")
    private UUID rapportVehiculeId;

    @Schema(description = "URL de l'image", example = "http://192.168.1.120:8081/uploads/rapports/890e1234_1234567890.jpg")
    private String pictureUrl;

    @Schema(description = "Date de création de la photo", example = "2025-01-15T10:35:00+01:00")
    private ZonedDateTime createdAt;
}
