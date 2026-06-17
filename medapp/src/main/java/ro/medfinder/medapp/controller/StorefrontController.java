package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.*;
import ro.medfinder.medapp.service.ClientOrderService;
import ro.medfinder.medapp.service.HoldingFeeCalculator;
import ro.medfinder.medapp.dto.OrderCreateRequest;

import java.security.Principal;
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
    private final ClientOrderService clientOrderService;

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

        List<ro.medfinder.medapp.dto.NearbyStockDto> stockDtos = stocks.stream()
                .map(stock -> ro.medfinder.medapp.dto.NearbyStockDto.builder()
                        .id(stock.getId())
                        .price(stock.getPrice())
                        .quantity(stock.getQuantity())
                        .location(ro.medfinder.medapp.dto.NearbyStockDto.LocationDto.builder()
                                .id(stock.getLocation().getId())
                                .name(stock.getLocation().getName())
                                .pharmacyName(stock.getLocation().getPharmacy() != null ? stock.getLocation().getPharmacy().getName() : "")
                                .address(stock.getLocation().getAddress())
                                .city(stock.getLocation().getCity())
                                .latitude(stock.getLocation().getLatitude())
                                .longitude(stock.getLocation().getLongitude())
                                .build())
                        .build())
                .toList();

        model.addAttribute("medication", medication);
        model.addAttribute("stocks", stockDtos);
        return "storefront/pdp";
    }

    @ResponseBody
    @GetMapping("/api/nearby")
    public ResponseEntity<List<ro.medfinder.medapp.dto.NearbyStockDto>> getNearbyStocks(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "10.0") Double radiusKm) {
        
        // Rough bounding box for ~1km (1 deg lat ~= 111km, 1 deg lng ~= 111km * cos(lat))
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lng - lngDelta;
        double maxLon = lng + lngDelta;

        List<MedStock> nearby = medStockRepository.findNearbyInStockOrderByLastSyncedDesc(minLat, maxLat, minLon, maxLon);
        
        List<ro.medfinder.medapp.dto.NearbyStockDto> dtos = nearby.stream().map(stock -> ro.medfinder.medapp.dto.NearbyStockDto.builder()
                .price(stock.getPrice())
                .quantity(stock.getQuantity())
                .medication(ro.medfinder.medapp.dto.NearbyStockDto.MedicationDto.builder()
                        .ean(stock.getMedication().getEan())
                        .name(stock.getMedication().getName())
                        .activeSubstance(stock.getMedication().getActiveSubstance())
                        .dosage(stock.getMedication().getDosage())
                        .prescriptionRequired(stock.getMedication().getPrescriptionRequired())
                        .form(stock.getMedication().getForm() != null ? stock.getMedication().getForm().name() : null)
                        .build())
                .location(ro.medfinder.medapp.dto.NearbyStockDto.LocationDto.builder()
                        .id(stock.getLocation().getId())
                        .name(stock.getLocation().getName())
                        .pharmacyName(stock.getLocation().getPharmacy() != null ? stock.getLocation().getPharmacy().getName() : "")
                        .address(stock.getLocation().getAddress())
                        .city(stock.getLocation().getCity())
                        .latitude(stock.getLocation().getLatitude())
                        .longitude(stock.getLocation().getLongitude())
                        .build())
                .build()).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/api/reserve")
    @ResponseBody
    public ResponseEntity<?> createReservation(
            @RequestParam Long locationId,
            @RequestParam Long medicationId,
            @RequestParam int quantity,
            @RequestParam int hours,
            Principal principal) {
            
        if (principal == null) return ResponseEntity.status(401).body("Please sign in to reserve");
        
        // Fetch real logged-in user
        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(principal.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Logged in client not found in DB"));

        OrderCreateRequest req = new OrderCreateRequest();
        req.setLocationId(locationId);
        req.setMedicationId(medicationId);
        req.setQuantity(quantity);
        req.setReservationHours(hours);

        try {
            Order order = clientOrderService.createOrder(client, req);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
