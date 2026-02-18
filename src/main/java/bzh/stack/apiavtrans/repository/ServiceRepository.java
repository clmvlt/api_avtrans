package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    List<Service> findByUser(User user);
    Page<Service> findByUser(User user, Pageable pageable);
    List<Service> findByUserAndDebutBetween(User user, ZonedDateTime start, ZonedDateTime end);
    Optional<Service> findByUserAndFinIsNull(User user);

    Optional<Service> findFirstByUserAndFinIsNullOrderByDebutDesc(User user);

    @Query("SELECT s FROM Service s WHERE s.user = :user " +
           "AND (CAST(:isBreak AS boolean) IS NULL OR s.isBreak = :isBreak) " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR s.debut >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR s.debut <= :endDate)")
    Page<Service> findByUserWithFilters(
            @Param("user") User user,
            @Param("isBreak") Boolean isBreak,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            Pageable pageable
    );

    void deleteByUser(User user);
}
