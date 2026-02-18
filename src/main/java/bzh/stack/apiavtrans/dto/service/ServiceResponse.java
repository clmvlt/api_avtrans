package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse standard pour les opérations sur les services")
public class ServiceResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message décrivant le résultat de l'opération", example = "Service started successfully")
    private String message;

    @Schema(description = "Données du service")
    private ServiceDTO service;
}
