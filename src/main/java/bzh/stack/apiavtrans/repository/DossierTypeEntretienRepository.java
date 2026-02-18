package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.DossierTypeEntretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierTypeEntretienRepository extends JpaRepository<DossierTypeEntretien, UUID> {
    Optional<DossierTypeEntretien> findByNom(String nom);
}
