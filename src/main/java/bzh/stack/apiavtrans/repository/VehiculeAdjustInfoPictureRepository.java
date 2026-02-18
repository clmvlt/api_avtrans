package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.VehiculeAdjustInfoPicture;
import bzh.stack.apiavtrans.entity.VehiculeAdjustInfo;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehiculeAdjustInfoPictureRepository extends JpaRepository<VehiculeAdjustInfoPicture, UUID> {
    List<VehiculeAdjustInfoPicture> findByAdjustInfo(VehiculeAdjustInfo adjustInfo);
    void deleteByAdjustInfo(VehiculeAdjustInfo adjustInfo);

    @Modifying
    @Query("DELETE FROM VehiculeAdjustInfoPicture p WHERE p.adjustInfo.user = :user")
    void deleteByAdjustInfoUser(@Param("user") User user);
}
