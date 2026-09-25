package bzh.stack.apiavtrans.dto.service;

import bzh.stack.apiavtrans.dto.common.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trace d'une action d'un administrateur sur un pointage")
public class ServiceModificationDTO {

    @Schema(description = "UUID de la trace")
    private UUID uuid;

    @Schema(description = "UUID du pointage concerné (peut ne plus exister si action = DELETE)")
    private UUID serviceUuid;

    @Schema(description = "Type d'action : CREATE, UPDATE ou DELETE", example = "UPDATE")
    private String action;

    @Schema(description = "Utilisateur dont le pointage a été touché")
    private UserDTO user;

    @Schema(description = "Administrateur ayant fait l'action (null si son compte a été supprimé)")
    private UserDTO modifiedBy;

    @Schema(description = "Début avant l'action (null pour CREATE)")
    private ZonedDateTime oldDebut;

    @Schema(description = "Fin avant l'action (null pour CREATE ou si le pointage était en cours)")
    private ZonedDateTime oldFin;

    @Schema(description = "Type avant l'action : true = pause, false = service (null pour CREATE)")
    private Boolean oldIsBreak;

    @Schema(description = "Début après l'action (null pour DELETE)")
    private ZonedDateTime newDebut;

    @Schema(description = "Fin après l'action (null pour DELETE ou si le pointage est en cours)")
    private ZonedDateTime newFin;

    @Schema(description = "Type après l'action : true = pause, false = service (null pour DELETE)")
    private Boolean newIsBreak;

    @Schema(description = "Date de l'action")
    private ZonedDateTime createdAt;
}
