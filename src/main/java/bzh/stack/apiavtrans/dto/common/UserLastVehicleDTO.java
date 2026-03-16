package bzh.stack.apiavtrans.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dernier véhicule utilisé par un utilisateur")
public class UserLastVehicleDTO {

    @Schema(description = "UUID de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userUuid;

    @Schema(description = "UUID du véhicule", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID vehiculeId;

    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String vehiculeImmat;

    @Schema(description = "Date de la dernière utilisation")
    private ZonedDateTime date;
}
