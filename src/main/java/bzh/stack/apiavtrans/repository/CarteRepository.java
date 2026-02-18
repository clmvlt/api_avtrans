package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Carte;
import bzh.stack.apiavtrans.entity.TypeCarte;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CarteRepository extends JpaRepository<Carte, UUID> {
    List<Carte> findByUser(User user);
    List<Carte> findByTypeCarte(TypeCarte typeCarte);
    List<Carte> findByUserOrderByCreatedAtDesc(User user);
    List<Carte> findByTypeCarteOrderByCreatedAtDesc(TypeCarte typeCarte);
    List<Carte> findAllByOrderByCreatedAtDesc();
    void deleteByUser(User user);
}
