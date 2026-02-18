package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.Absence.AbsenceStatus;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AbsenceSpecifications {

    public static Specification<Absence> withUser(User user) {
        return (root, query, cb) -> user == null ? null : cb.equal(root.get("user"), user);
    }

    public static Specification<Absence> withUserUuid(UUID userUuid) {
        return (root, query, cb) -> userUuid == null ? null : cb.equal(root.get("user").get("uuid"), userUuid);
    }

    public static Specification<Absence> startDateGreaterOrEqual(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    public static Specification<Absence> endDateLessOrEqual(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("endDate"), endDate);
    }

    public static Specification<Absence> withStatus(AbsenceStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Absence> withAbsenceType(UUID absenceTypeUuid) {
        return (root, query, cb) -> absenceTypeUuid == null ? null : cb.equal(root.get("absenceType").get("uuid"), absenceTypeUuid);
    }

    public static Specification<Absence> buildSearchSpec(User user, LocalDate startDate, LocalDate endDate,
                                                          AbsenceStatus status, UUID absenceTypeUuid) {
        return Specification.where(withUser(user))
                .and(startDateGreaterOrEqual(startDate))
                .and(endDateLessOrEqual(endDate))
                .and(withStatus(status))
                .and(withAbsenceType(absenceTypeUuid));
    }

    public static Specification<Absence> buildSearchSpec(User user, LocalDate startDate, LocalDate endDate,
                                                          AbsenceStatus status, UUID absenceTypeUuid, UUID userUuid) {
        return Specification.where(withUser(user))
                .and(withUserUuid(userUuid))
                .and(startDateGreaterOrEqual(startDate))
                .and(endDateLessOrEqual(endDate))
                .and(withStatus(status))
                .and(withAbsenceType(absenceTypeUuid));
    }
}
