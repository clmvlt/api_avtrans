package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Entretien;
import bzh.stack.apiavtrans.entity.EntretienFile;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EntretienFileRepository extends JpaRepository<EntretienFile, UUID> {
    List<EntretienFile> findByEntretienOrderByCreatedAtDesc(Entretien entretien);

    @Modifying
    @Query("DELETE FROM EntretienFile f WHERE f.entretien.mecanicien = :user")
    void deleteByEntretienMecanicien(@Param("user") User user);
}
