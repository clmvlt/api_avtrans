package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.EmailVerification;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findByToken(String token);
    void deleteByUser(User user);
}
