package ro.medfinder.medapp.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.entity.Client;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.ClientRepository;
import ro.medfinder.medapp.repository.OrderRepository;
import ro.medfinder.medapp.service.ClientOrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Cronjob care rulează la fiecare 5 minute și curăță comenzile:
 * 1. Expiră comenzile ACCEPTED/READY_FOR_PICKUP unde {@code acceptedAt + reservationHours < now}
 *    → status EXPIRED, stoc restaurat.
 * 2. Auto-reject comenzile PENDING mai vechi de 30 minute (farmacia nu a răspuns)
 *    → status REJECTED, perk gratuit returnat dacă a fost folosit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final ClientOrderService clientOrderService;
    private final ClientRepository clientRepository;

    private static final Set<OrderStatus> EXPIRABLE_STATUSES = Set.of(
            OrderStatus.ACCEPTED,
            OrderStatus.READY_FOR_PICKUP
    );

    /** Timeout după care o comandă PENDING e auto-rejected (farmacia nu a răspuns). */
    private static final int PENDING_TIMEOUT_MINUTES = 30;

    /**
     * Rulează la fiecare 5 minute (secunda 0, minutele 0, 5, 10, ...).
     */
    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
    public void processScheduledTasks() {
        expireOverdueOrders();
        autoRejectStalePendingOrders();
    }

    /**
     * Expiră comenzile ACCEPTED/READY_FOR_PICKUP care au depășit timpul de rezervare.
     */
    private void expireOverdueOrders() {
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

    /**
     * Auto-reject comenzile PENDING mai vechi de 30 minute.
     * Farmacia nu a răspuns → clientul primește reject automat + perk-ul înapoi.
     */
    private void autoRejectStalePendingOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(PENDING_TIMEOUT_MINUTES);

        List<Order> stalePending = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PENDING, cutoff);

        if (stalePending.isEmpty()) {
            return;
        }

        log.info("Found {} stale PENDING orders to auto-reject", stalePending.size());

        for (Order order : stalePending) {
            try {
                order.setStatus(OrderStatus.REJECTED);
                order.setRejectionReason("Timp de răspuns farmacie expirat (30 minute).");

                // Restaurare perk gratuit dacă a fost folosit
                if (Boolean.TRUE.equals(order.getUsedFreePerk())) {
                    Client client = order.getClient();
                    client.setFreeLongReservationsLeft(client.getFreeLongReservationsLeft() + 1);
                    clientRepository.save(client);
                    log.info("Restored free perk for client {} after auto-reject — now has {}",
                            client.getId(), client.getFreeLongReservationsLeft());
                }

                orderRepository.save(order);
                log.info("Order {} auto-rejected — pharmacy did not respond within {} minutes",
                        order.getOrderNumber(), PENDING_TIMEOUT_MINUTES);
            } catch (Exception e) {
                log.error("Failed to auto-reject order {}: {}", order.getOrderNumber(), e.getMessage(), e);
            }
        }

        log.info("Auto-reject job completed: {} orders processed", stalePending.size());
    }
}
