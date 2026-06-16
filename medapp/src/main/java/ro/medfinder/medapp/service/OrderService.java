package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.OrderStatusUpdate;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.Pharmacist;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

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

        if (newStatus == OrderStatus.REJECTED) {
            if (update.getRejectionReason() == null || update.getRejectionReason().isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            order.setRejectionReason(update.getRejectionReason());
        }

        if (newStatus == OrderStatus.PICKED_UP) {
            order.setPickedUpAt(LocalDateTime.now());
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }
}
