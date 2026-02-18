package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.TypeCarte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeCarteRepository extends JpaRepository<TypeCarte, UUID> {
    Optional<TypeCarte> findByNom(String nom);
}
