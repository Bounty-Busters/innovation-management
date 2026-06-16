package ro.medfinder.medapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.MedForm;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.*;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LocationRepository locationRepository;
    private final MedicationRepository medicationRepository;
    private final MedStockRepository medStockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already seeded
        }

        // 1. Super User
        User admin = User.builder()
                .email("admin@medfinder.ro")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("Super")
                .role(Role.SUPER_USER)
                .enabled(true)
                .build();
        userRepository.save(admin);

        // 2. Pharm Owner
        PharmOwner owner = PharmOwner.builder()
                .email("owner@medfinder.ro")
                .password(passwordEncoder.encode("owner123"))
                .firstName("Ion")
                .lastName("Proprietar")
                .role(Role.PHARM_OWNER)
                .enabled(true)
                .companyName("Farmacia Ta SRL")
                .build();
        userRepository.save(owner);

        // 3. Pharmacy
        Pharmacy pharmacy = Pharmacy.builder()
                .name("Catena")
                .cui("RO12345678")
                .phone("0700000001")
                .email("contact@catena.ro")
                .owner(owner)
                .active(true)
                .build();
        pharmacyRepository.save(pharmacy);

        // 4. Location
        Location location = Location.builder()
                .name("Catena Unirii")
                .address("Piata Unirii 1")
                .city("Bucuresti")
                .county("Bucuresti")
                .latitude(44.4268)
                .longitude(26.1025)
                .phone("0210000001")
                .pharmacy(pharmacy)
                .active(true)
                .build();
        locationRepository.save(location);

        // 5. Pharmacist
        Pharmacist pharmacist = Pharmacist.builder()
                .email("farmacist@medfinder.ro")
                .password(passwordEncoder.encode("pharma123"))
                .firstName("Maria")
                .lastName("Farmacist")
                .role(Role.PHARMACIST)
                .enabled(true)
                .location(location)
                .build();
        userRepository.save(pharmacist);

        // 6. Medications
        Medication paracetamol = Medication.builder()
                .ean("1234567890123")
                .name("Paracetamol")
                .activeSubstance("Paracetamolum")
                .form(MedForm.TABLET)
                .dosage("500mg")
                .category("Analgezice")
                .prescriptionRequired(false)
                .build();
        
        Medication nurofen = Medication.builder()
                .ean("9876543210987")
                .name("Nurofen Răceală și Gripă")
                .activeSubstance("Ibuprofen + Pseudoefedrină")
                .form(MedForm.TABLET)
                .dosage("200mg/30mg")
                .category("Antiinflamatoare")
                .prescriptionRequired(false)
                .build();

        medicationRepository.saveAll(List.of(paracetamol, nurofen));

        // 7. MedStock
        MedStock stock1 = MedStock.builder()
                .location(location)
                .medication(paracetamol)
                .quantity(150)
                .price(new BigDecimal("9.50"))
                .build();
        
        MedStock stock2 = MedStock.builder()
                .location(location)
                .medication(nurofen)
                .quantity(45)
                .price(new BigDecimal("25.99"))
                .build();

        medStockRepository.saveAll(List.of(stock1, stock2));

        System.out.println("✅ Seed data loaded successfully.");
    }
}
