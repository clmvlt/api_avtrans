package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import bzh.stack.apiavtrans.entity.Service;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ServiceMapper {

    public ServiceDTO toDTO(Service service) {
        if (service == null) {
            return null;
        }

        ServiceDTO dto = new ServiceDTO();
        dto.setUuid(service.getUuid());
        dto.setDebut(service.getDebut());
        dto.setFin(service.getFin());

        Long duree = service.getDuree();
        if (duree == null && service.getFin() == null && service.getDebut() != null) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
            duree = Duration.between(service.getDebut(), now).getSeconds();
        }
        dto.setDuree(duree);

        dto.setIsBreak(service.getIsBreak());
        dto.setLatitude(service.getLatitude());
        dto.setLongitude(service.getLongitude());
        dto.setLatitudeEnd(service.getLatitudeEnd());
        dto.setLongitudeEnd(service.getLongitudeEnd());
        dto.setIsAdmin(service.getIsAdmin());
        dto.setUserUuid(service.getUser() != null ? service.getUser().getUuid() : null);

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
}
