package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehiculeKilometrageRepository extends JpaRepository<VehiculeKilometrage, UUID> {
    List<VehiculeKilometrage> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule);

    @Modifying
    @Query("UPDATE VehiculeKilometrage vk SET vk.user = null WHERE vk.user = :user")
    void setUserToNull(@Param("user") User user);

    Page<VehiculeKilometrage> findByVehiculeOrderByCreatedAtDesc(Vehicule vehicule, Pageable pageable);

    @Query("SELECT vk FROM VehiculeKilometrage vk WHERE vk.vehicule = :vehicule ORDER BY vk.createdAt DESC LIMIT 1")
    Optional<VehiculeKilometrage> findLatestByVehicule(Vehicule vehicule);

    @Query("SELECT vk FROM VehiculeKilometrage vk WHERE vk.user = :user ORDER BY vk.createdAt DESC LIMIT 1")
    Optional<VehiculeKilometrage> findLatestByUser(@Param("user") User user);

    @Query("SELECT COUNT(vk) > 0 FROM VehiculeKilometrage vk WHERE vk.user = :user AND vk.createdAt >= :startOfDay AND vk.createdAt < :endOfDay")
    boolean existsByUserAndCreatedAtBetween(@Param("user") User user, @Param("startOfDay") ZonedDateTime startOfDay, @Param("endOfDay") ZonedDateTime endOfDay);
}
