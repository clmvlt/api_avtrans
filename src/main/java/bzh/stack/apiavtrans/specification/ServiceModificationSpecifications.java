package bzh.stack.apiavtrans.specification;

import bzh.stack.apiavtrans.entity.ServiceModification;
import bzh.stack.apiavtrans.entity.ServiceModification.Action;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ServiceModificationSpecifications {

    public static Specification<ServiceModification> withUserUuid(UUID userUuid) {
        return (root, query, criteriaBuilder) ->
                userUuid == null ? null : criteriaBuilder.equal(root.get("user").get("uuid"), userUuid);
    }

    public static Specification<ServiceModification> withModifiedByUuid(UUID modifiedByUuid) {
        return (root, query, criteriaBuilder) ->
                modifiedByUuid == null ? null : criteriaBuilder.equal(root.get("modifiedBy").get("uuid"), modifiedByUuid);
    }

    public static Specification<ServiceModification> withAction(Action action) {
        return (root, query, criteriaBuilder) ->
                action == null ? null : criteriaBuilder.equal(root.get("action"), action);
    }

    public static Specification<ServiceModification> createdAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) return null;
            ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneId.of("Europe/Paris"));
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime);
        };
    }

    public static Specification<ServiceModification> createdBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) return null;
            ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(ZoneId.of("Europe/Paris")).minusNanos(1);
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime);
        };
    }

    public static Specification<ServiceModification> buildSearchSpec(UUID userUuid, UUID modifiedByUuid, Action action,
                                                                     LocalDate startDate, LocalDate endDate) {
        return Specification.where(withUserUuid(userUuid))
                .and(withModifiedByUuid(modifiedByUuid))
                .and(withAction(action))
                .and(createdAfterOrEqual(startDate))
                .and(createdBeforeOrEqual(endDate));
    }
}
