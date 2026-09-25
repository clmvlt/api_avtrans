package bzh.stack.apiavtrans.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private UUID uuid;
    private ZonedDateTime debut;
    private ZonedDateTime fin;
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
}
