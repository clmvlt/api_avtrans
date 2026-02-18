package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.RapportVehicule;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RapportVehiculeRepository extends JpaRepository<RapportVehicule, UUID> {
    List<RapportVehicule> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule);

    Page<RapportVehicule> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule, Pageable pageable);

    @Query("SELECT rv FROM RapportVehicule rv WHERE rv.user = :user ORDER BY rv.createdAt DESC LIMIT 1")
    Optional<RapportVehicule> findLatestByUser(User user);

    void deleteByUser(User user);
}
