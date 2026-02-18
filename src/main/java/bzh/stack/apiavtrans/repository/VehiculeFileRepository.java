package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehiculeFileRepository extends JpaRepository<VehiculeFile, UUID> {
    List<VehiculeFile> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule);

    void deleteByVehicule(Vehicule vehicule);
}
