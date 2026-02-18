package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Entretien;
import bzh.stack.apiavtrans.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, UUID>, JpaSpecificationExecutor<Entretien> {
    List<Entretien> findByVehiculeOrderByDateEntretienDesc(Vehicule vehicule);

    @Query("SELECT e FROM Entretien e WHERE e.vehicule = :vehicule AND e.typeEntretien.id = :typeEntretienId ORDER BY e.dateEntretien DESC")
    List<Entretien> findByVehiculeAndTypeEntretienOrderByDateEntretienDesc(
        @Param("vehicule") Vehicule vehicule,
        @Param("typeEntretienId") UUID typeEntretienId
    );

    void deleteByMecanicien(bzh.stack.apiavtrans.entity.User mecanicien);
}
