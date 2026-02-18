package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Signature;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, UUID> {

    List<Signature> findByUserOrderByDateDesc(User user);

    Optional<Signature> findFirstByUserOrderByDateDesc(User user);

    @Query("SELECT s FROM Signature s WHERE s.user.uuid = :userUuid ORDER BY s.date DESC")
    List<Signature> findAllByUserUuidOrderByDateDesc(@Param("userUuid") UUID userUuid);

    @Query("SELECT s FROM Signature s WHERE s.user = :user ORDER BY s.date DESC LIMIT 1")
    Optional<Signature> findLatestByUser(@Param("user") User user);

    void deleteByUser(User user);
}
