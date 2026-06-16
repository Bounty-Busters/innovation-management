package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.*;
import ro.medfinder.medapp.service.HoldingFeeCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class StorefrontController {

    private final MedicationRepository medicationRepository;
    private final MedStockRepository medStockRepository;
    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final LocationRepository locationRepository;
    private final HoldingFeeCalculator holdingFeeCalculator;

    @GetMapping
    public String home(Model model) {
        // Will render the map and request GPS
        return "storefront/home";
    }

    @GetMapping("/m/{ean}")
    public String pdp(@PathVariable String ean, Model model) {
        Optional<Medication> medOpt = medicationRepository.findByEan(ean);
        if (medOpt.isEmpty()) {
            return "redirect:/";
        }
        Medication medication = medOpt.get();
        List<MedStock> stocks = medStockRepository.findByMedicationEanAndQuantityGreaterThan(ean, 0);

        model.addAttribute("medication", medication);
        model.addAttribute("stocks", stocks);
        return "storefront/pdp";
    }

    @ResponseBody
    @GetMapping("/api/nearby")
    public ResponseEntity<List<MedStock>> getNearbyStocks(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "1.0") Double radiusKm) {
        
        // Rough bounding box for ~1km (1 deg lat ~= 111km, 1 deg lng ~= 111km * cos(lat))
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lng - lngDelta;
        double maxLon = lng + lngDelta;

        List<MedStock> nearby = medStockRepository.findNearbyInStockOrderByLastSyncedDesc(minLat, maxLat, minLon, maxLon);
        return ResponseEntity.ok(nearby);
    }

    @PostMapping("/api/reserve")
    @ResponseBody
    public ResponseEntity<?> createReservation(
            @RequestParam Long locationId,
            @RequestParam Long medicationId,
            @RequestParam int quantity,
            @RequestParam int hours) {

        // 1. Mock currently logged in client (Client 1)
        Client client = clientRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No clients found"));

        // 2. Find location and medication
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));

        // 3. Check stock
        MedStock stock = medStockRepository.findByLocationIdAndMedicationId(locationId, medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        if (stock.getQuantity() < quantity) {
            return ResponseEntity.badRequest().body("Insufficient stock");
        }

        // 4. Calculate Holding Fee
        HoldingFeeCalculator.HoldingFeeResult feeResult = holdingFeeCalculator.calculate(hours, client);

        // Save client if perk was used
        if (feeResult.usedFreePerk()) {
            clientRepository.save(client);
        }

        // 5. Create Order
        Order order = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999))
                .client(client)
                .pickupLocation(location)
                .status(OrderStatus.PENDING)
                .totalPrice(feeResult.fee()) // Initially just the fee, we add items next
                .holdingFee(feeResult.fee())
                .usedFreePerk(feeResult.usedFreePerk())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(hours))
                .estimatedPickupTime(LocalDateTime.now().plusHours(hours))
                .build();

        orderRepository.save(order);

        // 6. Create Order Item
        BigDecimal subtotal = stock.getPrice().multiply(BigDecimal.valueOf(quantity));
        OrderItem item = OrderItem.builder()
                .order(order)
                .medication(medication)
                .quantity(quantity)
                .unitPrice(stock.getPrice())
                .subtotal(subtotal)
                .build();

        orderItemRepository.save(item);

        // Update total price (fee + subtotal)
        order.setTotalPrice(feeResult.fee().add(subtotal));
        orderRepository.save(order);

        // Note: Stock is NOT decreased yet. State machine logic in OrderService
        // says ACCEPTED decreases stock. PENDING just waits for pharmacy to accept.

        return ResponseEntity.ok(order);
    }
}
