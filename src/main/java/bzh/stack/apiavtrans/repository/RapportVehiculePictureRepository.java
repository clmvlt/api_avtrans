package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.RapportVehiculePicture;
import bzh.stack.apiavtrans.entity.RapportVehicule;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RapportVehiculePictureRepository extends JpaRepository<RapportVehiculePicture, UUID> {
    List<RapportVehiculePicture> findByRapportVehicule(RapportVehicule rapportVehicule);

    @Modifying
    @Query("DELETE FROM RapportVehiculePicture p WHERE p.rapportVehicule.user = :user")
    void deleteByRapportVehiculeUser(@Param("user") User user);
}
