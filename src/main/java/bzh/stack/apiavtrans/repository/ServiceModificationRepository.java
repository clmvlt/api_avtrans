package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.ServiceModification;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceModificationRepository extends JpaRepository<ServiceModification, UUID>, JpaSpecificationExecutor<ServiceModification> {

    @Override
    @EntityGraph(attributePaths = {"user", "user.role", "modifiedBy", "modifiedBy.role"})
    Page<ServiceModification> findAll(Specification<ServiceModification> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.role", "modifiedBy", "modifiedBy.role"})
    List<ServiceModification> findByServiceUuidOrderByCreatedAtDesc(UUID serviceUuid);

    void deleteByUser(User user);

    @Modifying
    @Query("UPDATE ServiceModification m SET m.modifiedBy = null WHERE m.modifiedBy = :user")
    void setModifiedByToNull(@Param("user") User user);
}
