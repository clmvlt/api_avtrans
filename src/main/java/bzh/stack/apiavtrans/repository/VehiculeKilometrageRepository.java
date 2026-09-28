package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
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

    /** Dernier relevé du véhicule lui-même : les relevés d'un véhicule relais sont ignorés. */
    @Query("SELECT vk FROM VehiculeKilometrage vk WHERE vk.vehicule = :vehicule AND vk.relai IS NULL ORDER BY vk.createdAt DESC LIMIT 1")
    Optional<VehiculeKilometrage> findLatestByVehicule(Vehicule vehicule);

    @Query("SELECT vk FROM VehiculeKilometrage vk WHERE vk.relai = :relai ORDER BY vk.createdAt DESC LIMIT 1")
    Optional<VehiculeKilometrage> findLatestByRelai(@Param("relai") VehiculeRelai relai);

    long countByRelai(VehiculeRelai relai);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE VehiculeKilometrage vk SET vk.relai = null WHERE vk.relai = :relai")
    int detacherDuRelai(@Param("relai") VehiculeRelai relai);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE VehiculeKilometrage vk SET vk.relai = null WHERE vk.vehicule = :vehicule AND vk.relai IS NOT NULL")
    int detacherDesRelais(@Param("vehicule") Vehicule vehicule);

    /** Rattache au relais les relevés du véhicule saisis dans [debut, fin[. */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE VehiculeKilometrage vk SET vk.relai = :relai WHERE vk.vehicule = :vehicule " +
           "AND vk.createdAt >= :debut AND vk.createdAt < :fin")
    int rattacherAuRelai(@Param("relai") VehiculeRelai relai,
                         @Param("vehicule") Vehicule vehicule,
                         @Param("debut") ZonedDateTime debut,
                         @Param("fin") ZonedDateTime fin);

    @Query("SELECT vk FROM VehiculeKilometrage vk WHERE vk.user = :user ORDER BY vk.createdAt DESC LIMIT 1")
    Optional<VehiculeKilometrage> findLatestByUser(@Param("user") User user);

    @Query("SELECT COUNT(vk) > 0 FROM VehiculeKilometrage vk WHERE vk.user = :user AND vk.createdAt >= :startOfDay AND vk.createdAt < :endOfDay")
    boolean existsByUserAndCreatedAtBetween(@Param("user") User user, @Param("startOfDay") ZonedDateTime startOfDay, @Param("endOfDay") ZonedDateTime endOfDay);

    @Query("SELECT vk FROM VehiculeKilometrage vk WHERE vk.user IS NOT NULL AND vk.user.isVisible = true AND vk.createdAt = " +
           "(SELECT MAX(vk2.createdAt) FROM VehiculeKilometrage vk2 WHERE vk2.user = vk.user)")
    List<VehiculeKilometrage> findLatestByEachUser();
}
