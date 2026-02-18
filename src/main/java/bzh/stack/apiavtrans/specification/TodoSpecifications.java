package bzh.stack.apiavtrans.specification;

import bzh.stack.apiavtrans.entity.Todo;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class TodoSpecifications {

    public static Specification<Todo> withCategory(UUID categoryUuid) {
        return (root, query, criteriaBuilder) ->
                categoryUuid == null ? null : criteriaBuilder.equal(root.get("category").get("uuid"), categoryUuid);
    }

    public static Specification<Todo> withIsDone(Boolean isDone) {
        return (root, query, criteriaBuilder) ->
                isDone == null ? null : criteriaBuilder.equal(root.get("isDone"), isDone);
    }

    public static Specification<Todo> createdAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) return null;
            ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneId.of("Europe/Paris"));
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime);
        };
    }

    public static Specification<Todo> createdBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) return null;
            ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(ZoneId.of("Europe/Paris")).minusNanos(1);
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime);
        };
    }

    public static Specification<Todo> buildSearchSpec(UUID categoryUuid, Boolean isDone,
                                                       LocalDate startDate, LocalDate endDate) {
        return Specification.where(withCategory(categoryUuid))
                .and(withIsDone(isDone))
                .and(createdAfterOrEqual(startDate))
                .and(createdBeforeOrEqual(endDate));
    }
}
