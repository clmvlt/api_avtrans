package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoCategoryRepository extends JpaRepository<TodoCategory, UUID> {

    Optional<TodoCategory> findByName(String name);

    boolean existsByName(String name);
}
