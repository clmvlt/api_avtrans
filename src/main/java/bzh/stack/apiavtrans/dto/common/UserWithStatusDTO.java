package bzh.stack.apiavtrans.dto.common;

import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Utilisateur avec son statut de présence actuel")
public class UserWithStatusDTO {
    @Schema(description = "UUID de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Schema(description = "Email de l'utilisateur", example = "jean.dupont@example.com")
    private String email;

    @Schema(description = "Prénom de l'utilisateur", example = "Jean")
    private String firstName;

    @Schema(description = "Nom de l'utilisateur", example = "Dupont")
    private String lastName;

    @Schema(description = "Rôle de l'utilisateur")
    private RoleDTO role;

    @Schema(description = "URL de la photo de profil", example = "http://192.168.1.120:8081/uploads/pictures/user.jpg")
    private String pictureUrl;

    @Schema(description = "Indique si l'utilisateur a une couchette", example = "false")
    private Boolean isCouchette;

    @Schema(description = "Statut de présence : PRESENT (en service), ON_BREAK (en pause), ABSENT (pas de service actif)",
            example = "PRESENT",
            allowableValues = {"PRESENT", "ON_BREAK", "ABSENT"})
    private String status;

    @Schema(description = "Service actif (si présent ou en pause)")
    private ServiceDTO activeService;

    @Schema(description = "Heures travaillées aujourd'hui", example = "5.5")
    private Double hoursToday;
}
