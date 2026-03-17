package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.AbsenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbsenceTypeRepository extends JpaRepository<AbsenceType, UUID> {

    Optional<AbsenceType> findByName(String name);

    boolean existsByName(String name);

    Optional<AbsenceType> findFirstByNameIgnoreCaseAndColorIsNotNull(String name);
}
