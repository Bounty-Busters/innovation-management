package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.medfinder.medapp.dto.DashboardChartData;
import ro.medfinder.medapp.dto.DashboardStats;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;
    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;
    private final MedStockRepository medStockRepository;

    public DashboardStats getStatsForUser(User user) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        if (user.getRole() == Role.SUPER_USER) {
            long ordersToday = orderRepository.findByCreatedAtAfter(todayStart).size();
            return DashboardStats.builder()
                    .label1("Total Users").value1(String.valueOf(userRepository.count()))
                    .label2("Total Pharmacies").value2(String.valueOf(pharmacyRepository.count()))
                    .label3("Total Medications").value3(String.valueOf(medicationRepository.count()))
                    .label4("Orders Today").value4(String.valueOf(ordersToday))
                    .build();

        } else if (user.getRole() == Role.PHARM_OWNER) {
            List<Order> ownerOrdersToday = orderRepository.findByPickupLocationPharmacyOwnerIdAndCreatedAtAfter(user.getId(), todayStart);
            BigDecimal revenueToday = ownerOrdersToday.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return DashboardStats.builder()
                    .label1("My Pharmacies").value1(String.valueOf(pharmacyRepository.findByOwnerId(user.getId()).size()))
                    .label2("My Locations").value2(String.valueOf(locationRepository.findByPharmacyOwnerId(user.getId()).size()))
                    .label3("Orders Today (Own)").value3(String.valueOf(ownerOrdersToday.size()))
                    .label4("Revenue Today").value4(revenueToday + " RON")
                    .build();

        } else if (user.getRole() == Role.PHARMACIST) {
            Long locId = ((Pharmacist) user).getLocation() != null ? ((Pharmacist) user).getLocation().getId() : -1L;
            
            List<Order> locOrdersToday = orderRepository.findByPickupLocationIdAndCreatedAtAfter(locId, todayStart);
            long pendingOrders = orderRepository.countByPickupLocationIdAndStatus(locId, OrderStatus.PENDING);
            long completedOrders = orderRepository.countByPickupLocationIdAndStatus(locId, OrderStatus.PICKED_UP);
            long stockItems = medStockRepository.countByLocationId(locId);

            return DashboardStats.builder()
                    .label1("Medications in Stock").value1(String.valueOf(stockItems))
                    .label2("Orders Today").value2(String.valueOf(locOrdersToday.size()))
                    .label3("Pending Orders").value3(String.valueOf(pendingOrders))
                    .label4("Completed Orders").value4(String.valueOf(completedOrders))
                    .build();
        }
        
        return DashboardStats.builder().build();
    }

    public DashboardChartData getChartDataForUser(User user) {
        LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(30).atStartOfDay();
        List<Order> orders;
        
        if (user.getRole() == Role.SUPER_USER) {
            orders = orderRepository.findByCreatedAtAfter(thirtyDaysAgo);
        } else if (user.getRole() == Role.PHARM_OWNER) {
            orders = orderRepository.findByPickupLocationPharmacyOwnerIdAndCreatedAtAfter(user.getId(), thirtyDaysAgo);
        } else {
            Long locId = ((Pharmacist) user).getLocation() != null ? ((Pharmacist) user).getLocation().getId() : -1L;
            orders = orderRepository.findByPickupLocationIdAndCreatedAtAfter(locId, thirtyDaysAgo);
        }

        // 1. Line Chart: Orders per day (last 30 days)
        Map<String, Long> ordersPerDay = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM")),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        
        // 2. Doughnut Chart: Orders per status
        Map<String, Long> ordersPerStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.counting()
                ));

        // 3 & 4: Role specific charts
        List<String> barLabels = new ArrayList<>();
        List<Integer> barData = new ArrayList<>();
        List<String> pieLabels = new ArrayList<>();
        List<Integer> pieData = new ArrayList<>();

        if (user.getRole() == Role.SUPER_USER) {
            // Bar: Top meds ordered (simplification: we group by status first, or we can just show active pharmacies)
            // Pie: Pharmacies per county
            List<Location> allLocs = locationRepository.findAll();
            Map<String, Long> locsPerCounty = allLocs.stream()
                    .filter(l -> l.getCounty() != null)
                    .collect(Collectors.groupingBy(Location::getCounty, Collectors.counting()));
            
            pieLabels = new ArrayList<>(locsPerCounty.keySet());
            pieData = locsPerCounty.values().stream().map(Long::intValue).collect(Collectors.toList());
            
            // Bar: Let's do revenue per last 7 days
            LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay();
            Map<String, Double> revenuePerDay = orders.stream()
                    .filter(o -> o.getCreatedAt().isAfter(sevenDaysAgo))
                    .collect(Collectors.groupingBy(
                            o -> o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM")),
                            LinkedHashMap::new,
                            Collectors.summingDouble(o -> o.getTotalPrice().doubleValue())
                    ));
            barLabels = new ArrayList<>(revenuePerDay.keySet());
            barData = revenuePerDay.values().stream().map(Double::intValue).collect(Collectors.toList());

        } else if (user.getRole() == Role.PHARM_OWNER) {
            // Bar: Revenue per pharmacy
            Map<String, Double> revenuePerLoc = orders.stream()
                    .filter(o -> o.getPickupLocation() != null)
                    .collect(Collectors.groupingBy(
                            o -> o.getPickupLocation().getPharmacy().getName(),
                            Collectors.summingDouble(o -> o.getTotalPrice().doubleValue())
                    ));
            barLabels = new ArrayList<>(revenuePerLoc.keySet());
            barData = revenuePerLoc.values().stream().map(Double::intValue).collect(Collectors.toList());
            
            // Pie: low stock alert (own)
            List<MedStock> lowStock = medStockRepository.findByLocationPharmacyOwnerIdAndQuantityLessThanEqual(user.getId(), 20);
            pieLabels = lowStock.stream().map(m -> m.getMedication().getName()).limit(5).collect(Collectors.toList());
            pieData = lowStock.stream().map(MedStock::getQuantity).limit(5).collect(Collectors.toList());
            
        } else if (user.getRole() == Role.PHARMACIST) {
            Long locId = ((Pharmacist) user).getLocation() != null ? ((Pharmacist) user).getLocation().getId() : -1L;
            List<MedStock> lowStock = medStockRepository.findByLocationIdAndQuantityLessThanEqual(locId, 20);
            barLabels = lowStock.stream().map(m -> m.getMedication().getName()).limit(10).collect(Collectors.toList());
            barData = lowStock.stream().map(MedStock::getQuantity).limit(10).collect(Collectors.toList());
        }

        return DashboardChartData.builder()
                .lineLabels(new ArrayList<>(ordersPerDay.keySet()))
                .lineData(ordersPerDay.values().stream().map(Long::intValue).collect(Collectors.toList()))
                .doughnutLabels(new ArrayList<>(ordersPerStatus.keySet()))
                .doughnutData(ordersPerStatus.values().stream().map(Long::intValue).collect(Collectors.toList()))
                .barLabels(barLabels)
                .barData(barData)
                .pieLabels(pieLabels)
                .pieData(pieData)
                .build();
    }
}
