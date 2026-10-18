package bzh.stack.apiavtrans.dto.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pointage : service de travail (isBreak=false, avec ses pauses) ou pause (isBreak=true, rattachée à son service). "
        + "Les durées sont calculées à l'instant calculeLe ; pour un affichage en direct, ajouter le temps écoulé "
        + "depuis la réception : à duree si enCours, à dureeTravail si enCours et pas enPause, à dureePauses si enPause.")
public class ServiceDTO {
    private UUID uuid;
    private ZonedDateTime debut;
    private ZonedDateTime fin;

    @Schema(description = "Durée brute de la ligne en secondes (fin − début, ou calculeLe − début si en cours)", example = "28800")
    private Long duree;

    private Boolean isBreak;
    private Double latitude;
    private Double longitude;
    private Double latitudeEnd;
    private Double longitudeEnd;
    private Boolean isAdmin;
    private UUID userUuid;

    @Schema(description = "Date de la dernière modification par un administrateur (null si jamais modifié)")
    private ZonedDateTime modifiedAt;

    @Schema(description = "UUID de l'administrateur ayant fait la dernière modification")
    private UUID modifiedByUuid;

    @Schema(description = "Prénom et nom de l'administrateur ayant fait la dernière modification", example = "Jean Dupont")
    private String modifiedByName;

    @Schema(description = "Instant de calcul des durées (heure serveur)")
    private ZonedDateTime calculeLe;

    @Schema(description = "true si la ligne n'a pas de fin : duree augmente d'une seconde par seconde", example = "true")
    private Boolean enCours;

    @Schema(description = "Pause uniquement : UUID du service de travail auquel elle appartient")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID serviceParentUuid;

    @Schema(description = "Service uniquement : pauses du service, par début croissant (une pause après minuit reste dans son service)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ServiceDTO> pauses;

    @Schema(description = "Service uniquement : durée des pauses en secondes, ramenées dans les bornes du service", example = "3600")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long dureePauses;

    @Schema(description = "Service uniquement : temps de travail net en secondes (duree − dureePauses)", example = "25200")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long dureeTravail;

    @Schema(description = "Service uniquement : true si le service est en cours avec une pause en cours", example = "false")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enPause;
}
