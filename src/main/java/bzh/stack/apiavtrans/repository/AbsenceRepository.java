package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, UUID>, JpaSpecificationExecutor<Absence> {

    List<Absence> findByUserOrderByStartDateDesc(User user);

    Page<Absence> findByUserOrderByStartDateDesc(User user, Pageable pageable);

    @Query("SELECT a FROM Absence a WHERE a.user = :user AND " +
           "((a.startDate <= :endDate AND a.endDate >= :startDate))")
    List<Absence> findOverlappingAbsences(@Param("user") User user,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Absence a WHERE a.user.uuid = :userUuid AND " +
           "a.startDate >= :startDate AND a.endDate <= :endDate")
    List<Absence> findByUserUuidAndDateRange(@Param("userUuid") UUID userUuid,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    List<Absence> findByAbsenceType(AbsenceType absenceType);

    void deleteByUser(User user);

    @Query("UPDATE Absence a SET a.validatedBy = null WHERE a.validatedBy = :user")
    @org.springframework.data.jpa.repository.Modifying
    void setValidatedByToNull(@Param("user") User user);
}
