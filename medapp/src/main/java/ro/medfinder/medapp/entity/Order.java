package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ro.medfinder.medapp.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comandă Click & Collect.
 * Flux: PENDING → ACCEPTED → READY_FOR_PICKUP → PICKED_UP
 *       (+ ramuri: REJECTED, CANCELLED, EXPIRED)
 *
 * holdingFee = 0 dacă ridicare ≤ 2h, altfel se calculează automat.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"client", "pickupLocation", "items"})
@EqualsAndHashCode(callSuper = true, exclude = {"client", "pickupLocation", "items"})
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "holding_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal holdingFee = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // ── Relații ──────────────────────────────────────────────────

    /** Clientul care a plasat comanda. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    /** Locația de unde se ridică comanda. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_location_id")
    private Location pickupLocation;

    /** Produsele din comandă. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
