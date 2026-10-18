package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.TempsTravailCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceMapper {

    private final TempsTravailCalculator tempsTravailCalculator;

    /**
     * Pointage avec ses durées calculées à {@code calculeLe}.
     *
     * @param pauses pauses du service de travail (ignoré pour une pause)
     */
    public ServiceDTO toDTO(Service service, List<Service> pauses, ZonedDateTime calculeLe) {
        if (service == null) {
            return null;
        }

        ServiceDTO dto = new ServiceDTO();
        dto.setUuid(service.getUuid());
        dto.setDebut(service.getDebut());
        dto.setFin(service.getFin());
        dto.setDuree(tempsTravailCalculator.dureeBrute(service, calculeLe));
        dto.setIsBreak(service.getIsBreak());
        dto.setLatitude(service.getLatitude());
        dto.setLongitude(service.getLongitude());
        dto.setLatitudeEnd(service.getLatitudeEnd());
        dto.setLongitudeEnd(service.getLongitudeEnd());
        dto.setIsAdmin(service.getIsAdmin());
        dto.setUserUuid(service.getUser() != null ? service.getUser().getUuid() : null);
        dto.setModifiedAt(service.getModifiedAt());
        dto.setCalculeLe(calculeLe);
        dto.setEnCours(service.getFin() == null);

        User modifiedBy = service.getModifiedBy();
        if (modifiedBy != null) {
            dto.setModifiedByUuid(modifiedBy.getUuid());
            dto.setModifiedByName(fullName(modifiedBy));
        }

        if (Boolean.TRUE.equals(service.getIsBreak())) {
            dto.setServiceParentUuid(service.getServiceParent() != null ? service.getServiceParent().getUuid() : null);
        } else {
            List<Service> pausesDuService = pauses != null ? pauses : List.of();
            dto.setPauses(pausesDuService.stream().map(pause -> toDTO(pause, null, calculeLe)).toList());
            dto.setDureePauses(tempsTravailCalculator.dureePauses(service, pausesDuService, calculeLe));
            dto.setDureeTravail(tempsTravailCalculator.dureeTravail(service, pausesDuService, calculeLe));
            dto.setEnPause(tempsTravailCalculator.enPause(service, pausesDuService));
        }

        return dto;
    }

    public Service toEntity(ServiceDTO dto) {
        if (dto == null) {
            return null;
        }

        Service service = new Service();
        service.setUuid(dto.getUuid());
        service.setDebut(dto.getDebut());
        service.setFin(dto.getFin());
        service.setDuree(dto.getDuree());
        service.setIsBreak(dto.getIsBreak());
        service.setLatitude(dto.getLatitude());
        service.setLongitude(dto.getLongitude());
        service.setLatitudeEnd(dto.getLatitudeEnd());
        service.setLongitudeEnd(dto.getLongitudeEnd());
        service.setIsAdmin(dto.getIsAdmin());

        return service;
    }

    private String fullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
}
