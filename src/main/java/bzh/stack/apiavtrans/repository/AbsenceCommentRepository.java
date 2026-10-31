package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.AbsenceComment;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AbsenceCommentRepository extends JpaRepository<AbsenceComment, UUID> {

    /** Nombre de commentaires d'une absence (les absences sans commentaire n'ont pas de ligne). */
    interface AbsenceCommentCount {
        UUID getAbsenceUuid();

        long getCommentsCount();
    }

    @Query("SELECT c FROM AbsenceComment c LEFT JOIN FETCH c.author a LEFT JOIN FETCH a.role " +
           "WHERE c.absence.uuid = :absenceUuid ORDER BY c.createdAt ASC, c.uuid ASC")
    List<AbsenceComment> findByAbsenceUuidInOrder(@Param("absenceUuid") UUID absenceUuid);

    @Query("SELECT c.absence.uuid AS absenceUuid, COUNT(c) AS commentsCount FROM AbsenceComment c " +
           "WHERE c.absence.uuid IN :absenceUuids GROUP BY c.absence.uuid")
    List<AbsenceCommentCount> countByAbsenceUuids(@Param("absenceUuids") Collection<UUID> absenceUuids);

    /** Dernier commentaire de chaque absence, en une requête (les absences sans commentaire n'ont pas de ligne). */
    @Query("SELECT c FROM AbsenceComment c LEFT JOIN FETCH c.author a LEFT JOIN FETCH a.role " +
           "WHERE c.absence.uuid IN :absenceUuids AND c.createdAt = " +
           "(SELECT MAX(o.createdAt) FROM AbsenceComment o WHERE o.absence = c.absence)")
    List<AbsenceComment> findLatestByAbsenceUuids(@Param("absenceUuids") Collection<UUID> absenceUuids);

    @Modifying
    @Query("DELETE FROM AbsenceComment c WHERE c.absence.uuid = :absenceUuid")
    void deleteByAbsenceUuid(@Param("absenceUuid") UUID absenceUuid);

    /** Commentaires des absences d'un utilisateur, à supprimer avant ces absences. */
    @Modifying
    @Query("DELETE FROM AbsenceComment c WHERE c.absence.uuid IN " +
           "(SELECT a.uuid FROM Absence a WHERE a.user = :user)")
    void deleteByAbsenceUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE AbsenceComment c SET c.author = null WHERE c.author = :user")
    void setAuthorToNull(@Param("user") User user);
}
