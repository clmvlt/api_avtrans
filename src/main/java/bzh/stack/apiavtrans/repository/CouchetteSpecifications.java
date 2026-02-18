package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Couchette;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class CouchetteSpecifications {

    public static Specification<Couchette> withUser(User user) {
        return (root, query, cb) -> user == null ? null : cb.equal(root.get("user"), user);
    }

    public static Specification<Couchette> withUserUuid(UUID userUuid) {
        return (root, query, cb) -> userUuid == null ? null : cb.equal(root.get("user").get("uuid"), userUuid);
    }

    public static Specification<Couchette> dateGreaterOrEqual(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    public static Specification<Couchette> dateLessOrEqual(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("date"), endDate);
    }

    public static Specification<Couchette> buildSearchSpec(UUID userUuid, LocalDate startDate, LocalDate endDate) {
        return Specification.where(withUserUuid(userUuid))
                .and(dateGreaterOrEqual(startDate))
                .and(dateLessOrEqual(endDate));
    }
}
