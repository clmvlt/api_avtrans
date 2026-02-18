package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Couchette;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouchetteRepository extends JpaRepository<Couchette, UUID>, JpaSpecificationExecutor<Couchette> {

    Optional<Couchette> findByUserAndDate(User user, LocalDate date);

    boolean existsByUserAndDate(User user, LocalDate date);

    Page<Couchette> findAllByOrderByDateDescCreatedAtDesc(Pageable pageable);

    Page<Couchette> findByUserOrderByDateDescCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT c FROM Couchette c WHERE c.user.uuid = :userUuid ORDER BY c.date DESC, c.createdAt DESC")
    Page<Couchette> findByUserUuidOrderByDateDesc(@Param("userUuid") UUID userUuid, Pageable pageable);

    void deleteByUser(User user);
}
