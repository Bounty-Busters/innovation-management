package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    List<Order> findByPickupLocationIdOrderByCreatedAtDesc(Long locationId);
}
