package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.Todo;
import bzh.stack.apiavtrans.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID>, JpaSpecificationExecutor<Todo> {

    List<Todo> findByCategory(TodoCategory category);

    long countByCategory(TodoCategory category);
}
