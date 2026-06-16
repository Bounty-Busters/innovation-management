package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
