package ro.medfinder.medapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.OrderCreateRequest;
import ro.medfinder.medapp.entity.Client;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.repository.ClientRepository;
import ro.medfinder.medapp.service.ClientOrderService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller pentru operațiile Click & Collect din perspectiva clientului.
 *
 * Rute:
 * - POST   /api/client/orders           → creare comandă (rezervare)
 * - PUT    /api/client/orders/{id}/cancel → anulare comandă
 * - GET    /api/client/orders           → listare comenzi proprii
 */
@Slf4j
@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientOrderService clientOrderService;
    private final ClientRepository clientRepository;

    // ── POST /api/client/orders ─────────────────────────────────

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {
        try {
            Client client = (Client) userDetails.getUser();
            Order order = clientOrderService.createOrder(client, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toOrderResponse(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // ── PUT /api/client/orders/{id}/cancel ──────────────────────

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        try {
            Client client = (Client) userDetails.getUser();
            Order order = clientOrderService.cancelOrder(client, id);
            return ResponseEntity.ok(toOrderResponse(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/client/orders ──────────────────────────────────

    // ── Settings ────────────────────────────────────────────────

    @GetMapping("/settings/radius")
    public ResponseEntity<Map<String, Integer>> getSearchRadius(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Client client = (Client) userDetails.getUser();
        return ResponseEntity.ok(Map.of("radius", client.getSearchRadius() != null ? client.getSearchRadius() : -1));
    }

    @PostMapping("/settings/radius")
    public ResponseEntity<?> setSearchRadius(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer val) {
        Client client = (Client) userDetails.getUser();
        client.setSearchRadius(val);
        clientRepository.save(client);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> listOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Client client = (Client) userDetails.getUser();
        List<Order> orders = clientOrderService.getClientOrders(client);
        List<Map<String, Object>> response = orders.stream()
                .map(this::toOrderResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ── Response Mapper ─────────────────────────────────────────

    /**
     * Convertește un Order într-un Map simplu pentru răspunsul JSON.
     * Evită referințe circulare și expune doar ce trebuie.
     */
    private Map<String, Object> toOrderResponse(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("orderNumber", order.getOrderNumber());
        map.put("status", order.getStatus().name());
        map.put("totalPrice", order.getTotalPrice());
        map.put("holdingFee", order.getHoldingFee());
        map.put("reservationHours", order.getReservationHours());
        map.put("usedFreePerk", order.getUsedFreePerk());
        map.put("createdAt", order.getCreatedAt());
        map.put("acceptedAt", order.getAcceptedAt());
        map.put("notes", order.getNotes());
        map.put("rejectionReason", order.getRejectionReason());

        if (order.getPickupLocation() != null) {
            Map<String, Object> loc = new LinkedHashMap<>();
            loc.put("id", order.getPickupLocation().getId());
            loc.put("name", order.getPickupLocation().getName());
            loc.put("address", order.getPickupLocation().getAddress());
            map.put("pickupLocation", loc);
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<Map<String, Object>> items = order.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("medicationId", item.getMedication().getId());
                itemMap.put("medicationName", item.getMedication().getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("subtotal", item.getSubtotal());
                return itemMap;
            }).toList();
            map.put("items", items);
        }

        return map;
    }
}
