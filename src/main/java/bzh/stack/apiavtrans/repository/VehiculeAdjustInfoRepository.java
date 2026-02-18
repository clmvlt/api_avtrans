package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.VehiculeAdjustInfo;
import bzh.stack.apiavtrans.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehiculeAdjustInfoRepository extends JpaRepository<VehiculeAdjustInfo, UUID> {
    List<VehiculeAdjustInfo> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule);

    Page<VehiculeAdjustInfo> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule, Pageable pageable);

    void deleteByUser(bzh.stack.apiavtrans.entity.User user);
}
