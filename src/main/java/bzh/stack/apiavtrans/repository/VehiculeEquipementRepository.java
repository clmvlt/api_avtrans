package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehiculeEquipementRepository extends JpaRepository<VehiculeEquipement, UUID> {
    List<VehiculeEquipement> findByVehiculeOrderByNomAsc(Vehicule vehicule);
}
