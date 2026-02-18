package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Acompte;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcompteRepository extends JpaRepository<Acompte, UUID>, JpaSpecificationExecutor<Acompte> {

    List<Acompte> findByUserOrderByCreatedAtDesc(User user);

    Page<Acompte> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    void deleteByUser(User user);

    @org.springframework.data.jpa.repository.Query("UPDATE Acompte a SET a.validatedBy = null WHERE a.validatedBy = :user")
    @org.springframework.data.jpa.repository.Modifying
    void setValidatedByToNull(@org.springframework.data.repository.query.Param("user") User user);
}
