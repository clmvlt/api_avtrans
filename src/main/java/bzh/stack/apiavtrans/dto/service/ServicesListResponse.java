package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant une liste de services")
public class ServicesListResponse {
    @Schema(description = "Indique si l'opération a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message d'erreur si applicable")
    private String message;

    @Schema(description = "Liste des services")
    private List<ServiceDTO> services;

    public ServicesListResponse(Boolean success, List<ServiceDTO> services) {
        this.success = success;
        this.services = services;
    }
}
