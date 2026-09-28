package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehiculeRelaiRepository extends JpaRepository<VehiculeRelai, UUID> {

    List<VehiculeRelai> findByVehiculeOrderByDateDebutDesc(Vehicule vehicule);

    boolean existsByVehicule(Vehicule vehicule);

    /** Relais du véhicule en cours à cette date (au plus un : les périodes ne se chevauchent pas). */
    @Query("SELECT r FROM VehiculeRelai r WHERE r.vehicule = :vehicule AND r.dateDebut <= :date " +
           "AND (r.dateFin IS NULL OR r.dateFin >= :date) ORDER BY r.dateDebut DESC LIMIT 1")
    Optional<VehiculeRelai> findActifLe(@Param("vehicule") Vehicule vehicule, @Param("date") LocalDate date);

    /** Relais de tous les véhicules en cours à cette date. */
    @Query("SELECT r FROM VehiculeRelai r WHERE r.dateDebut <= :date AND (r.dateFin IS NULL OR r.dateFin >= :date)")
    List<VehiculeRelai> findAllActifsLe(@Param("date") LocalDate date);

    /**
     * Autres relais du véhicule qui chevauchent la période [debut, fin] (fin incluse). Pour une
     * période sans fin, passer une date lointaine.
     */
    @Query("SELECT r FROM VehiculeRelai r WHERE r.vehicule = :vehicule AND r.id <> :excludeId " +
           "AND r.dateDebut <= :fin AND (r.dateFin IS NULL OR r.dateFin >= :debut) ORDER BY r.dateDebut ASC")
    List<VehiculeRelai> findChevauchements(@Param("vehicule") Vehicule vehicule,
                                           @Param("excludeId") UUID excludeId,
                                           @Param("debut") LocalDate debut,
                                           @Param("fin") LocalDate fin);
}
