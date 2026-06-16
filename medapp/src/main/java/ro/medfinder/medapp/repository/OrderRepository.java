package ro.medfinder.medapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    List<Order> findByPickupLocationIdOrderByCreatedAtDesc(Long locationId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByPickupLocationPharmacyOwnerId(Long ownerId, Pageable pageable);

    Page<Order> findByPickupLocationPharmacyOwnerIdAndStatus(Long ownerId, OrderStatus status, Pageable pageable);

    Page<Order> findByPickupLocationId(Long locationId, Pageable pageable);

    Page<Order> findByPickupLocationIdAndStatus(Long locationId, OrderStatus status, Pageable pageable);

    long countByPickupLocationPharmacyOwnerId(Long ownerId);

    long countByPickupLocationId(Long locationId);

    List<Order> findByClientId(Long clientId);

    // Phase 3: Dashboard Stats & Charts
    List<Order> findByCreatedAtAfter(java.time.LocalDateTime date);
    List<Order> findByPickupLocationPharmacyOwnerIdAndCreatedAtAfter(Long ownerId, java.time.LocalDateTime date);
    List<Order> findByPickupLocationIdAndCreatedAtAfter(Long locationId, java.time.LocalDateTime date);
    
    long countByPickupLocationIdAndStatus(Long locationId, OrderStatus status);
}
