package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.OrderCreateRequest;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.ClientRepository;
import ro.medfinder.medapp.repository.LocationRepository;
import ro.medfinder.medapp.repository.MedStockRepository;
import ro.medfinder.medapp.repository.MedicationRepository;
import ro.medfinder.medapp.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service dedicat operațiilor Click & Collect din perspectiva clientului:
 * creare comandă, anulare, listare.
 *
 * Separat de {@link OrderService} (admin) pentru separarea responsabilităților.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientOrderService {

    private final OrderRepository orderRepository;
    private final MedStockRepository medStockRepository;
    private final MedicationRepository medicationRepository;
    private final LocationRepository locationRepository;
    private final ClientRepository clientRepository;
    private final HoldingFeeCalculator holdingFeeCalculator;

    /** Statusuri considerate „active" — blochează o nouă comandă pe aceeași combinație azi. */
    private static final Set<OrderStatus> ACTIVE_STATUSES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.ACCEPTED,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.PICKED_UP
    );

    /** Statusuri din care clientul poate anula. */
    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.ACCEPTED,
            OrderStatus.READY_FOR_PICKUP
    );

    // ── Creare Comandă ──────────────────────────────────────────

    /**
     * Creează o comandă Click & Collect cu status PENDING.
     * Nu scade stocul — stocul se scade doar la ACCEPTED (de farmacist).
     */
    @Transactional
    public Order createOrder(Client client, OrderCreateRequest req) {
        // 1. Lookup entități
        Medication medication = medicationRepository.findById(req.getMedicationId())
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with ID: " + req.getMedicationId()));

        Location location = locationRepository.findById(req.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + req.getLocationId()));

        MedStock medStock = medStockRepository.findByLocationIdAndMedicationId(req.getLocationId(), req.getMedicationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medication not available at this location"));

        // Verificare disponibilitate și cantitate suficientă
        if (!Boolean.TRUE.equals(medStock.getAvailable()) || medStock.getQuantity() < req.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + medStock.getQuantity());
        }

        // 2. Validare zilnică (anti-hoarding): max 1 comandă activă/zi per med+loc
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean alreadyExists = orderRepository.existsActiveOrderToday(
                client.getId(), req.getLocationId(), req.getMedicationId(),
                ACTIVE_STATUSES, startOfDay);

        if (alreadyExists) {
            throw new IllegalStateException(
                    "You already have an active reservation for this medication at this location today.");
        }

        // 3. Calcul holding fee (cu posibilă consumare perk gratuit)
        HoldingFeeCalculator.HoldingFeeResult feeResult =
                holdingFeeCalculator.calculate(req.getReservationHours(), client);

        // 4. Calcul total price
        BigDecimal itemSubtotal = medStock.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        BigDecimal totalPrice = itemSubtotal.add(feeResult.fee());

        // 5. Generare order number
        String orderNumber = generateOrderNumber();

        // 6. Construire Order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .holdingFee(feeResult.fee())
                .reservationHours(req.getReservationHours())
                .usedFreePerk(feeResult.usedFreePerk())
                .client(client)
                .pickupLocation(location)
                .build();

        // 7. Construire OrderItem (snapshot preț)
        OrderItem item = OrderItem.builder()
                .order(order)
                .medication(medication)
                .quantity(req.getQuantity())
                .unitPrice(medStock.getPrice())
                .subtotal(itemSubtotal)
                .build();

        order.getItems().add(item);

        // 8. Salvare (client dacă perk consumat + order)
        if (feeResult.usedFreePerk()) {
            clientRepository.save(client);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} created for client {} — status PENDING, holdingFee={}, usedFreePerk={}",
                orderNumber, client.getId(), feeResult.fee(), feeResult.usedFreePerk());

        return savedOrder;
    }

    // ── Anulare Comandă ─────────────────────────────────────────

    /**
     * Anulează o comandă a clientului.
     * Dacă statusul era ACCEPTED/READY_FOR_PICKUP, restaurează stocul.
     * Dacă s-a folosit un perk gratuit, îl returnează.
     */
    @Transactional
    public Order cancelOrder(Client client, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Verificare proprietate
        if (!order.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("This order does not belong to you");
        }

        // Verificare status
        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalStateException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

        // Restaurare stoc dacă a fost scăzut (ACCEPTED sau READY_FOR_PICKUP)
        if (order.getStatus() == OrderStatus.ACCEPTED
                || order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            restoreStock(order);
        }

        // Restaurare perk gratuit dacă a fost folosit
        if (Boolean.TRUE.equals(order.getUsedFreePerk())) {
            client.setFreeLongReservationsLeft(client.getFreeLongReservationsLeft() + 1);
            clientRepository.save(client);
            log.info("Restored free perk for client {} — now has {}",
                    client.getId(), client.getFreeLongReservationsLeft());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled by client {}", order.getOrderNumber(), client.getId());

        return saved;
    }

    // ── Listare Comenzi ─────────────────────────────────────────

    /**
     * Returnează comenzile clientului, ordonate descrescător după dată.
     */
    @Transactional(readOnly = true)
    public List<Order> getClientOrders(Client client) {
        return orderRepository.findByClientIdOrderByCreatedAtDesc(client.getId());
    }

    // ── Helpers ─────────────────────────────────────────────────

    /**
     * Restaurează stocul din MedStock pentru toate item-urile unei comenzi.
     */
    public void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            MedStock medStock = medStockRepository.findByLocationIdAndMedicationId(
                            order.getPickupLocation().getId(),
                            item.getMedication().getId())
                    .orElse(null);

            if (medStock != null) {
                medStock.setQuantity(medStock.getQuantity() + item.getQuantity());
                medStockRepository.save(medStock);
                log.info("Restored {} units of medication {} at location {}",
                        item.getQuantity(), item.getMedication().getId(),
                        order.getPickupLocation().getId());
            }
        }
    }

    private String generateOrderNumber() {
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD-" + timestamp + "-" + random;
    }
}
