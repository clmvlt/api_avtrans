package bzh.stack.apiavtrans.specification;

import bzh.stack.apiavtrans.entity.Acompte;
import bzh.stack.apiavtrans.entity.Acompte.AcompteStatus;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class AcompteSpecifications {

    public static Specification<Acompte> withUser(User user) {
        return (root, query, criteriaBuilder) ->
                user == null ? null : criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<Acompte> createdAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) return null;
            ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneId.of("Europe/Paris"));
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime);
        };
    }

    public static Specification<Acompte> createdBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) return null;
            ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(ZoneId.of("Europe/Paris")).minusNanos(1);
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime);
        };
    }

    public static Specification<Acompte> withStatus(AcompteStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Acompte> withMontantMin(Double montantMin) {
        return (root, query, criteriaBuilder) ->
                montantMin == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("montant"), montantMin);
    }

    public static Specification<Acompte> withMontantMax(Double montantMax) {
        return (root, query, criteriaBuilder) ->
                montantMax == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("montant"), montantMax);
    }

    public static Specification<Acompte> withUserUuid(UUID userUuid) {
        return (root, query, criteriaBuilder) ->
                userUuid == null ? null : criteriaBuilder.equal(root.get("user").get("uuid"), userUuid);
    }

    public static Specification<Acompte> withIsPaid(Boolean isPaid) {
        return (root, query, criteriaBuilder) ->
                isPaid == null ? null : criteriaBuilder.equal(root.get("isPaid"), isPaid);
    }

    public static Specification<Acompte> buildSearchSpec(User user, LocalDate startDate, LocalDate endDate,
                                                          AcompteStatus status, Double montantMin, Double montantMax, Boolean isPaid) {
        return Specification.where(withUser(user))
                .and(createdAfterOrEqual(startDate))
                .and(createdBeforeOrEqual(endDate))
                .and(withStatus(status))
                .and(withMontantMin(montantMin))
                .and(withMontantMax(montantMax))
                .and(withIsPaid(isPaid));
    }

    public static Specification<Acompte> buildAdminSearchSpec(UUID userUuid, LocalDate startDate, LocalDate endDate,
                                                                AcompteStatus status, Double montantMin, Double montantMax, Boolean isPaid) {
        return Specification.where(withUserUuid(userUuid))
                .and(createdAfterOrEqual(startDate))
                .and(createdBeforeOrEqual(endDate))
                .and(withStatus(status))
                .and(withMontantMin(montantMin))
                .and(withMontantMax(montantMax))
                .and(withIsPaid(isPaid));
    }
}
