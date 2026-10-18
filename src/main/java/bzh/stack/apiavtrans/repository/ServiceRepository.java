package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findByUserAndDebutBetween(User user, ZonedDateTime start, ZonedDateTime end);

    /** Services de travail (hors pauses) débutés dans [debut, fin[. */
    @Query("SELECT s FROM Service s WHERE s.user = :user AND s.isBreak = false " +
           "AND s.debut >= :debut AND s.debut < :fin ORDER BY s.debut ASC")
    List<Service> findServicesTravail(@Param("user") User user,
                                      @Param("debut") ZonedDateTime debut,
                                      @Param("fin") ZonedDateTime fin);

    @Query("SELECT COUNT(s) > 0 FROM Service s WHERE s.user = :user AND s.isBreak = false " +
           "AND s.debut >= :debut AND s.debut < :fin")
    boolean existsServiceTravail(@Param("user") User user,
                                 @Param("debut") ZonedDateTime debut,
                                 @Param("fin") ZonedDateTime fin);

    /** Pauses de plusieurs services en une seule requête. */
    @Query("SELECT p FROM Service p WHERE p.serviceParent IN :services ORDER BY p.debut ASC")
    List<Service> findPausesDe(@Param("services") Collection<Service> services);

    List<Service> findByServiceParentOrderByDebutAsc(Service serviceParent);

    /** Service de travail en cours (une pause en cours ne remplace pas son service). */
    Optional<Service> findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(User user);

    Optional<Service> findFirstByServiceParentAndFinIsNullOrderByDebutDesc(Service serviceParent);

    /** Service de travail du salarié en cours à l'instant donné (pour rattacher une pause saisie par un admin). */
    @Query("SELECT s FROM Service s WHERE s.user = :user AND s.isBreak = false " +
           "AND s.debut <= :instant AND (s.fin IS NULL OR s.fin >= :instant) ORDER BY s.debut DESC")
    List<Service> findServicesTravailContenant(@Param("user") User user, @Param("instant") ZonedDateTime instant);

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

    /** Suppression des pauses avant les services (clé étrangère service_parent_uuid). */
    @Modifying
    @Query("DELETE FROM Service s WHERE s.user = :user AND s.isBreak = true")
    void deletePausesByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Service s WHERE s.user = :user")
    void deleteServicesByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE Service s SET s.modifiedBy = null WHERE s.modifiedBy = :user")
    void setModifiedByToNull(@Param("user") User user);
}
