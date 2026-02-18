package bzh.stack.apiavtrans.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a notification")
public class NotificationCreateRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Notification title", example = "New maintenance scheduled")
    private String title;

    @NotBlank(message = "Description is required")
    @Schema(description = "Notification description", example = "Vehicle XYZ requires maintenance on 2025-12-15")
    private String description;

    @Schema(description = "Reference type (e.g., 'entretien', 'absence', 'acompte')", example = "entretien")
    private String refType;

    @Schema(description = "Reference ID (UUID as string)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String refId;
}
