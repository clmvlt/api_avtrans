package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.StockCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockCategoryRepository extends JpaRepository<StockCategory, UUID> {

    Optional<StockCategory> findByNom(String nom);

    List<StockCategory> findAllByOrderByNomAsc();
}
