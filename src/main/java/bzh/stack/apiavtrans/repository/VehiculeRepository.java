package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, UUID> {
    Optional<Vehicule> findByImmat(String immat);
    List<Vehicule> findAllByOrderByImmatAsc();
}
