package ro.medfinder.medapp.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.OrderRepository;
import ro.medfinder.medapp.service.ClientOrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Cronjob care verifică și expiră comenzile ale căror timp de rezervare a trecut.
 *
 * Rulează la fiecare 5 minute. Caută comenzile ACCEPTED sau READY_FOR_PICKUP
 * unde {@code acceptedAt + reservationHours < now} și le setează pe EXPIRED,
 * restaurând stocul în MedStock.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final ClientOrderService clientOrderService;

    private static final Set<OrderStatus> EXPIRABLE_STATUSES = Set.of(
            OrderStatus.ACCEPTED,
            OrderStatus.READY_FOR_PICKUP
    );

    /**
     * Rulează la fiecare 5 minute (secunda 0, minutele 0, 5, 10, ...).
     */
    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
    public void expireOverdueOrders() {
        LocalDateTime now = LocalDateTime.now();

        // Fetch toate comenzile candidate, filtrăm expirarea în Java
        List<Order> candidates = orderRepository.findByStatusInAndAcceptedAtNotNull(EXPIRABLE_STATUSES);

        List<Order> expiredOrders = candidates.stream()
                .filter(o -> o.getReservationHours() != null
                        && o.getAcceptedAt().plusHours(o.getReservationHours()).isBefore(now))
                .toList();

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} overdue orders to expire", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                // Restaurare stoc
                clientOrderService.restoreStock(order);

                // Status → EXPIRED
                order.setStatus(OrderStatus.EXPIRED);
                orderRepository.save(order);

                log.info("Order {} expired — stock restored, status set to EXPIRED",
                        order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to expire order {}: {}", order.getOrderNumber(), e.getMessage(), e);
            }
        }

        log.info("Expiration job completed: {} orders processed", expiredOrders.size());
    }
}
