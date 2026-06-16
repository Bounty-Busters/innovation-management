package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.OrderStatusUpdate;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.ClientRepository;
import ro.medfinder.medapp.repository.MedStockRepository;
import ro.medfinder.medapp.repository.OrderRepository;
import ro.medfinder.medapp.service.ClientOrderService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Service pentru gestionarea comenzilor din perspectiva admin/farmacist.
 * Gestionează tranzițiile de status și efectele lor (stock, perk-uri).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MedStockRepository medStockRepository;
    private final ClientRepository clientRepository;
    private final ClientOrderService clientOrderService;

    /** Valid status transitions per the state machine. */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.ACCEPTED, OrderStatus.REJECTED, OrderStatus.CANCELLED),
            OrderStatus.ACCEPTED, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED),
            OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.PICKED_UP, OrderStatus.EXPIRED)
    );

    public Page<Order> getOrdersForUser(User user, OrderStatus statusFilter, Pageable pageable) {
        if (user.getRole() == Role.SUPER_USER) {
            if (statusFilter != null) {
                return orderRepository.findByStatus(statusFilter, pageable);
            }
            return orderRepository.findAll(pageable);
        } else if (user.getRole() == Role.PHARM_OWNER) {
            if (statusFilter != null) {
                return orderRepository.findByPickupLocationPharmacyOwnerIdAndStatus(user.getId(), statusFilter, pageable);
            }
            return orderRepository.findByPickupLocationPharmacyOwnerId(user.getId(), pageable);
        } else if (user.getRole() == Role.PHARMACIST) {
            Pharmacist pharmacist = (Pharmacist) user;
            if (pharmacist.getLocation() != null) {
                if (statusFilter != null) {
                    return orderRepository.findByPickupLocationIdAndStatus(pharmacist.getLocation().getId(), statusFilter, pageable);
                }
                return orderRepository.findByPickupLocationId(pharmacist.getLocation().getId(), pageable);
            }
            return Page.empty(pageable);
        }
        return Page.empty(pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Transactional
    public void updateOrderStatus(Long id, OrderStatusUpdate update) {
        Order order = getOrderById(id);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = update.getNewStatus();

        Set<OrderStatus> validNext = VALID_TRANSITIONS.get(currentStatus);
        if (validNext == null || !validNext.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // ── ACCEPTED: scade stocul, setează acceptedAt ──────────
        if (newStatus == OrderStatus.ACCEPTED) {
            handleAccepted(order);
        }

        // ── REJECTED: dă perk-ul înapoi dacă a fost folosit ────
        if (newStatus == OrderStatus.REJECTED) {
            if (update.getRejectionReason() == null || update.getRejectionReason().isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            order.setRejectionReason(update.getRejectionReason());
            handleRejected(order);
        }

        // ── CANCELLED (de farmacist): restaurare stoc + perk ────
        if (newStatus == OrderStatus.CANCELLED) {
            handleCancelled(order);
        }

        if (newStatus == OrderStatus.PICKED_UP) {
            order.setPickedUpAt(LocalDateTime.now());
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Order {} status updated: {} → {}", order.getOrderNumber(), currentStatus, newStatus);
    }

    // ── Private Helpers ─────────────────────────────────────────

    /**
     * La ACCEPTED: scade stocul din MedStock pentru fiecare item.
     * Dacă stocul e insuficient, aruncă excepție (rollback tranzacție).
     */
    private void handleAccepted(Order order) {
        order.setAcceptedAt(LocalDateTime.now());

        for (OrderItem item : order.getItems()) {
            MedStock medStock = medStockRepository.findByLocationIdAndMedicationId(
                            order.getPickupLocation().getId(),
                            item.getMedication().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "MedStock not found for medication " + item.getMedication().getId()
                                    + " at location " + order.getPickupLocation().getId()));

            if (medStock.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for " + item.getMedication().getName()
                                + ". Available: " + medStock.getQuantity()
                                + ", requested: " + item.getQuantity());
            }

            medStock.setQuantity(medStock.getQuantity() - item.getQuantity());
            medStockRepository.save(medStock);
            log.info("Stock decreased: medication {} at location {} — {} units",
                    item.getMedication().getId(), order.getPickupLocation().getId(), item.getQuantity());
        }
    }

    /**
     * La REJECTED din PENDING: dacă s-a folosit un perk gratuit la creare,
     * îl returnează clientului.
     */
    private void handleRejected(Order order) {
        if (Boolean.TRUE.equals(order.getUsedFreePerk())) {
            Client client = order.getClient();
            client.setFreeLongReservationsLeft(client.getFreeLongReservationsLeft() + 1);
            clientRepository.save(client);
            log.info("Restored free perk for client {} after rejection — now has {}",
                    client.getId(), client.getFreeLongReservationsLeft());
        }
    }

    /**
     * La CANCELLED de farmacist: dacă comanda era ACCEPTED/READY_FOR_PICKUP,
     * restaurează stocul. Dacă s-a folosit un perk gratuit, îl returnează.
     */
    private void handleCancelled(Order order) {
        // Restaurare stoc doar dacă a fost scăzut (ACCEPTED sau READY_FOR_PICKUP)
        if (order.getStatus() == OrderStatus.ACCEPTED
                || order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            clientOrderService.restoreStock(order);
            log.info("Stock restored for cancelled order {}", order.getOrderNumber());
        }

        // Restaurare perk gratuit
        if (Boolean.TRUE.equals(order.getUsedFreePerk())) {
            Client client = order.getClient();
            client.setFreeLongReservationsLeft(client.getFreeLongReservationsLeft() + 1);
            clientRepository.save(client);
            log.info("Restored free perk for client {} after pharmacist cancel — now has {}",
                    client.getId(), client.getFreeLongReservationsLeft());
        }
    }
}
