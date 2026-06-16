package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.medfinder.medapp.dto.DashboardStats;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.LocationRepository;
import ro.medfinder.medapp.repository.MedicationRepository;
import ro.medfinder.medapp.repository.OrderRepository;
import ro.medfinder.medapp.repository.PharmacyRepository;
import ro.medfinder.medapp.repository.UserRepository;
import ro.medfinder.medapp.entity.enums.OrderStatus;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;
    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;

    public DashboardStats getStatsForUser(User user) {
        if (user.getRole() == Role.SUPER_USER) {
            return DashboardStats.builder()
                    .label1("Total Users").value1(String.valueOf(userRepository.count()))
                    .label2("Total Pharmacies").value2(String.valueOf(pharmacyRepository.count()))
                    .label3("Total Medications").value3(String.valueOf(medicationRepository.count()))
                    .label4("Total Orders").value4(String.valueOf(orderRepository.count()))
                    .build();
        } else if (user.getRole() == Role.PHARM_OWNER) {
            return DashboardStats.builder()
                    .label1("My Pharmacies").value1(String.valueOf(pharmacyRepository.findByOwnerId(user.getId()).size()))
                    .label2("My Locations").value2(String.valueOf(locationRepository.findByPharmacyOwnerId(user.getId()).size()))
                    .label3("Orders Today (Own)").value3("0") // placeholder
                    .label4("Revenue Today").value4("0 RON") // placeholder
                    .build();
        } else if (user.getRole() == Role.PHARMACIST) {
            return DashboardStats.builder()
                    .label1("Medications in Stock").value1("0") // placeholder
                    .label2("Orders Today").value2("0") // placeholder
                    .label3("Pending Orders").value3("0") // placeholder
                    .label4("Completed Orders").value4("0") // placeholder
                    .build();
        }
        
        return DashboardStats.builder().build();
    }
}
