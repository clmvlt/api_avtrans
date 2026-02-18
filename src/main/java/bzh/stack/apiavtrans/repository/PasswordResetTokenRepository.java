package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.PasswordResetToken;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByEmailAndIsUsedFalse(String email);
    void deleteByUser(User user);
}
