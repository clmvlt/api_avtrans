package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.TypeEntretien;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehiculeTypeEntretienRepository extends JpaRepository<VehiculeTypeEntretien, UUID> {
    List<VehiculeTypeEntretien> findByVehiculeAndActifTrue(Vehicule vehicule);
    List<VehiculeTypeEntretien> findByVehicule(Vehicule vehicule);
    Optional<VehiculeTypeEntretien> findByVehiculeAndTypeEntretien(Vehicule vehicule, TypeEntretien typeEntretien);
}
