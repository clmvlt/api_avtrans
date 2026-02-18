package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Notification;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);

    Long countByUserAndIsRead(User user, Boolean isRead);

    void deleteByUser(User user);
}
