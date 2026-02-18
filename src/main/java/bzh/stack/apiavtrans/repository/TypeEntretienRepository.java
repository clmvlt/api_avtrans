package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.TypeEntretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeEntretienRepository extends JpaRepository<TypeEntretien, UUID> {
    Optional<TypeEntretien> findByNom(String nom);
}
