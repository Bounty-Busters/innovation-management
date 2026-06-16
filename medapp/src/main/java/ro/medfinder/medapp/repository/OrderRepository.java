package ro.medfinder.medapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Collection;
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

    // ── Click & Collect: daily limit check ──────────────────────
    /** Verifică dacă există deja o comandă activă azi pentru același client/locație/medicament. */
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items oi " +
           "WHERE o.client.id = :clientId " +
           "AND o.pickupLocation.id = :locationId " +
           "AND oi.medication.id = :medicationId " +
           "AND o.status IN :statuses " +
           "AND o.createdAt >= :since")
    boolean existsActiveOrderToday(@Param("clientId") Long clientId,
                                   @Param("locationId") Long locationId,
                                   @Param("medicationId") Long medicationId,
                                   @Param("statuses") Collection<OrderStatus> statuses,
                                   @Param("since") LocalDateTime since);

    // ── Click & Collect: expiration cronjob ─────────────────────
    /** Găsește comenzile acceptate/ready cu acceptedAt setat (filtrarea exactă se face în Java). */
    @Query("SELECT o FROM Order o " +
           "WHERE o.status IN :statuses " +
           "AND o.acceptedAt IS NOT NULL")
    List<Order> findByStatusInAndAcceptedAtNotNull(@Param("statuses") Collection<OrderStatus> statuses);

    // ── Click & Collect: client orders listing ──────────────────
    List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
