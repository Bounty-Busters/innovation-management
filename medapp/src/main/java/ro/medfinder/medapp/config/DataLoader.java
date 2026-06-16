package ro.medfinder.medapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.MedForm;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LocationRepository locationRepository;
    private final MedicationRepository medicationRepository;
    private final MedStockRepository medStockRepository;
    private final WorkingHourRepository workingHourRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already seeded
        }

        // ── 1. SUPER_USER ──────────────────────────────────────────
        User admin = User.builder()
                .email("admin@medfinder.ro")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("Super")
                .role(Role.SUPER_USER)
                .enabled(true)
                .build();
        userRepository.save(admin);

        // ── 2. PHARM_OWNERS ────────────────────────────────────────
        PharmOwner owner1 = PharmOwner.builder()
                .email("owner@medfinder.ro")
                .password(passwordEncoder.encode("owner123"))
                .firstName("Ion")
                .lastName("Proprietar")
                .role(Role.PHARM_OWNER)
                .enabled(true)
                .companyName("Farmacia Ta SRL")
                .build();
        userRepository.save(owner1);

        PharmOwner owner2 = PharmOwner.builder()
                .email("owner2@medfinder.ro")
                .password(passwordEncoder.encode("owner123"))
                .firstName("Elena")
                .lastName("Ionescu")
                .role(Role.PHARM_OWNER)
                .enabled(true)
                .companyName("Pharma Plus SRL")
                .build();
        userRepository.save(owner2);

        // ── 3. PHARMACIES ──────────────────────────────────────────
        Pharmacy catena = Pharmacy.builder()
                .name("Catena")
                .cui("RO12345678")
                .phone("0700000001")
                .email("contact@catena.ro")
                .website("https://www.catena.ro")
                .owner(owner1)
                .active(true)
                .syncEnabled(true)
                .build();
        pharmacyRepository.save(catena);

        Pharmacy drMax = Pharmacy.builder()
                .name("Dr. Max")
                .cui("RO87654321")
                .phone("0700000002")
                .email("contact@drmax.ro")
                .website("https://www.drmax.ro")
                .owner(owner1)
                .active(true)
                .build();
        pharmacyRepository.save(drMax);

        Pharmacy farmaTei = Pharmacy.builder()
                .name("Farmacia Tei")
                .cui("RO11223344")
                .phone("0700000003")
                .email("contact@farmaciatei.ro")
                .owner(owner2)
                .active(true)
                .build();
        pharmacyRepository.save(farmaTei);

        // ── 4. LOCATIONS ───────────────────────────────────────────
        Location catenaUnirii = Location.builder()
                .name("Catena Unirii")
                .address("Piata Unirii 1")
                .city("Bucuresti")
                .county("Bucuresti")
                .postalCode("030031")
                .latitude(44.4268)
                .longitude(26.1025)
                .phone("0210000001")
                .pharmacy(catena)
                .active(true)
                .build();
        locationRepository.save(catenaUnirii);

        Location catenaVictoriei = Location.builder()
                .name("Catena Victoriei")
                .address("Calea Victoriei 100")
                .city("Bucuresti")
                .county("Bucuresti")
                .postalCode("010072")
                .latitude(44.4400)
                .longitude(26.0960)
                .phone("0210000002")
                .pharmacy(catena)
                .active(true)
                .build();
        locationRepository.save(catenaVictoriei);

        Location drMaxCluj = Location.builder()
                .name("Dr. Max Cluj")
                .address("Str. Memorandumului 10")
                .city("Cluj-Napoca")
                .county("Cluj")
                .postalCode("400114")
                .latitude(46.7712)
                .longitude(23.5912)
                .phone("0264000001")
                .pharmacy(drMax)
                .active(true)
                .build();
        locationRepository.save(drMaxCluj);

        Location farmaTeiTimisoara = Location.builder()
                .name("Farmacia Tei Timișoara")
                .address("Bd. Revoluției 5")
                .city("Timișoara")
                .county("Timiș")
                .postalCode("300024")
                .latitude(45.7489)
                .longitude(21.2087)
                .phone("0256000001")
                .pharmacy(farmaTei)
                .active(true)
                .build();
        locationRepository.save(farmaTeiTimisoara);

        // ── 5. WORKING HOURS ───────────────────────────────────────
        createDefaultWorkingHours(catenaUnirii);
        createDefaultWorkingHours(catenaVictoriei);
        createDefaultWorkingHours(drMaxCluj);
        createDefaultWorkingHours(farmaTeiTimisoara);

        // ── 6. PHARMACISTS ─────────────────────────────────────────
        Pharmacist pharmacist1 = Pharmacist.builder()
                .email("farmacist@medfinder.ro")
                .password(passwordEncoder.encode("pharma123"))
                .firstName("Maria")
                .lastName("Farmacist")
                .role(Role.PHARMACIST)
                .enabled(true)
                .location(catenaUnirii)
                .build();
        userRepository.save(pharmacist1);

        Pharmacist pharmacist2 = Pharmacist.builder()
                .email("farmacist2@medfinder.ro")
                .password(passwordEncoder.encode("pharma123"))
                .firstName("Andrei")
                .lastName("Popescu")
                .role(Role.PHARMACIST)
                .enabled(true)
                .location(catenaVictoriei)
                .build();
        userRepository.save(pharmacist2);

        Pharmacist pharmacist3 = Pharmacist.builder()
                .email("farmacist3@medfinder.ro")
                .password(passwordEncoder.encode("pharma123"))
                .firstName("Ana")
                .lastName("Dumitrescu")
                .role(Role.PHARMACIST)
                .enabled(true)
                .location(drMaxCluj)
                .build();
        userRepository.save(pharmacist3);

        Pharmacist pharmacist4 = Pharmacist.builder()
                .email("farmacist4@medfinder.ro")
                .password(passwordEncoder.encode("pharma123"))
                .firstName("Mihai")
                .lastName("Popa")
                .role(Role.PHARMACIST)
                .enabled(true)
                .location(farmaTeiTimisoara)
                .build();
        userRepository.save(pharmacist4);

        // ── 7. CLIENTS ─────────────────────────────────────────────
        Client client1 = Client.builder()
                .email("client1@gmail.com")
                .password(passwordEncoder.encode("client123"))
                .firstName("Alexandru")
                .lastName("Gheorghe")
                .phone("0722000001")
                .role(Role.CLIENT)
                .enabled(true)
                .city("Bucuresti")
                .deliveryAddress("Str. Floreasca 22")
                .build();
        userRepository.save(client1);

        Client client2 = Client.builder()
                .email("client2@gmail.com")
                .password(passwordEncoder.encode("client123"))
                .firstName("Diana")
                .lastName("Marinescu")
                .phone("0733000002")
                .role(Role.CLIENT)
                .enabled(true)
                .city("Cluj-Napoca")
                .deliveryAddress("Str. Dorobanților 15")
                .build();
        userRepository.save(client2);

        Client client3 = Client.builder()
                .email("client3@gmail.com")
                .password(passwordEncoder.encode("client123"))
                .firstName("Radu")
                .lastName("Stanescu")
                .phone("0744000003")
                .role(Role.CLIENT)
                .enabled(true)
                .city("Timișoara")
                .deliveryAddress("Str. Mihai Viteazu 10")
                .build();
        userRepository.save(client3);

        // ── 8. MEDICATIONS ─────────────────────────────────────────
        Medication paracetamol = createMed("5941732000010", "Paracetamol 500mg", "Paracetamolum", MedForm.TABLET, "500mg", "Analgezice", false);
        Medication nurofen = createMed("5941732000027", "Nurofen Răceală și Gripă", "Ibuprofen + Pseudoefedrină", MedForm.TABLET, "200mg/30mg", "Antiinflamatoare", false);
        Medication amoxicilina = createMed("5941732000034", "Amoxicilina 500mg", "Amoxicillinum", MedForm.CAPSULE, "500mg", "Antibiotice", true);
        Medication augmentin = createMed("5941732000041", "Augmentin 1g", "Amoxicilină + Acid clavulanic", MedForm.TABLET, "875mg/125mg", "Antibiotice", true);
        Medication metformin = createMed("5941732000058", "Metformin 850mg", "Metforminum", MedForm.TABLET, "850mg", "Antidiabetice", true);
        Medication amlodipina = createMed("5941732000065", "Amlodipina 5mg", "Amlodipinum", MedForm.TABLET, "5mg", "Cardiovasculare", true);
        Medication omeprazol = createMed("5941732000072", "Omeprazol 20mg", "Omeprazolum", MedForm.CAPSULE, "20mg", "Gastroenterologie", false);
        Medication vitamina_c = createMed("5941732000089", "Vitamina C 1000mg", "Acidum ascorbicum", MedForm.OTHER, "1000mg", "Vitamine", false);
        Medication strepsils = createMed("5941732000096", "Strepsils Intensive", "Flurbiprofenul", MedForm.OTHER, "8.75mg", "ORL", false);
        Medication aspirin = createMed("5941732000102", "Aspirină Cardio 100mg", "Acidum acetylsalicylicum", MedForm.TABLET, "100mg", "Cardiovasculare", false);
        Medication brufen = createMed("5941732000119", "Brufen 400mg", "Ibuprofenum", MedForm.TABLET, "400mg", "Antiinflamatoare", false);
        Medication colebil = createMed("5941732000126", "Colebil", "Extractum cynare", MedForm.TABLET, "70mg", "Hepatoprotectoare", false);
        Medication spasmomen = createMed("5941732000133", "Spasmomen 40mg", "Otilonium", MedForm.TABLET, "40mg", "Gastroenterologie", true);
        Medication claritine = createMed("5941732000140", "Claritine 10mg", "Loratadinum", MedForm.TABLET, "10mg", "Antialergice", false);
        Medication coldrex = createMed("5941732000157", "Coldrex MaxGrip", "Paracetamol + Fenilefrină + Acid ascorbic", MedForm.OTHER, "1000mg/10mg/40mg", "Antigripale", false);

        medicationRepository.saveAll(List.of(paracetamol, nurofen, amoxicilina, augmentin, metformin,
                amlodipina, omeprazol, vitamina_c, strepsils, aspirin, brufen, colebil, spasmomen, claritine, coldrex));

        // ── 9. MED STOCKS ──────────────────────────────────────────
        createStock(catenaUnirii, paracetamol, 150, "9.50");
        createStock(catenaUnirii, nurofen, 45, "25.99");
        createStock(catenaUnirii, amoxicilina, 30, "18.50");
        createStock(catenaUnirii, omeprazol, 80, "22.00");
        createStock(catenaUnirii, vitamina_c, 200, "12.99");
        createStock(catenaUnirii, aspirin, 120, "15.00");

        createStock(catenaVictoriei, paracetamol, 90, "9.50");
        createStock(catenaVictoriei, augmentin, 25, "42.00");
        createStock(catenaVictoriei, metformin, 60, "28.50");
        createStock(catenaVictoriei, claritine, 40, "19.99");

        createStock(drMaxCluj, nurofen, 70, "24.99");
        createStock(drMaxCluj, amoxicilina, 35, "17.50");
        createStock(drMaxCluj, amlodipina, 50, "32.00");
        createStock(drMaxCluj, strepsils, 100, "16.50");
        createStock(drMaxCluj, coldrex, 85, "14.99");

        createStock(farmaTeiTimisoara, paracetamol, 110, "8.99");
        createStock(farmaTeiTimisoara, brufen, 55, "21.00");
        createStock(farmaTeiTimisoara, colebil, 40, "35.00");
        createStock(farmaTeiTimisoara, spasmomen, 30, "45.00");
        createStock(farmaTeiTimisoara, vitamina_c, 150, "11.99");

        // ── 10. ORDERS ─────────────────────────────────────────────
        Order order1 = createOrder("ORD-20260615-0001", client1, catenaUnirii, OrderStatus.PENDING,
                new BigDecimal("35.49"), LocalDateTime.now().minusHours(2));
        createOrderItem(order1, paracetamol, 2, new BigDecimal("9.50"));
        createOrderItem(order1, vitamina_c, 1, new BigDecimal("12.99"));
        updateOrderTotal(order1);

        Order order2 = createOrder("ORD-20260615-0002", client1, catenaUnirii, OrderStatus.ACCEPTED,
                BigDecimal.ZERO, LocalDateTime.now().minusHours(5));
        createOrderItem(order2, nurofen, 1, new BigDecimal("25.99"));
        createOrderItem(order2, aspirin, 1, new BigDecimal("15.00"));
        updateOrderTotal(order2);

        Order order3 = createOrder("ORD-20260614-0003", client2, drMaxCluj, OrderStatus.READY_FOR_PICKUP,
                BigDecimal.ZERO, LocalDateTime.now().minusDays(1));
        createOrderItem(order3, amoxicilina, 1, new BigDecimal("17.50"));
        createOrderItem(order3, strepsils, 2, new BigDecimal("16.50"));
        updateOrderTotal(order3);

        Order order4 = createOrder("ORD-20260613-0004", client2, drMaxCluj, OrderStatus.PICKED_UP,
                BigDecimal.ZERO, LocalDateTime.now().minusDays(2));
        order4.setPickedUpAt(LocalDateTime.now().minusDays(1).minusHours(3));
        createOrderItem(order4, coldrex, 3, new BigDecimal("14.99"));
        updateOrderTotal(order4);
        orderRepository.save(order4);

        Order order5 = createOrder("ORD-20260612-0005", client3, farmaTeiTimisoara, OrderStatus.REJECTED,
                BigDecimal.ZERO, LocalDateTime.now().minusDays(3));
        order5.setRejectionReason("Medication out of stock — supplier delay");
        createOrderItem(order5, spasmomen, 2, new BigDecimal("45.00"));
        updateOrderTotal(order5);
        orderRepository.save(order5);

        Order order6 = createOrder("ORD-20260611-0006", client3, farmaTeiTimisoara, OrderStatus.CANCELLED,
                BigDecimal.ZERO, LocalDateTime.now().minusDays(4));
        createOrderItem(order6, brufen, 1, new BigDecimal("21.00"));
        updateOrderTotal(order6);

        Order order7 = createOrder("ORD-20260616-0007", client1, catenaVictoriei, OrderStatus.PENDING,
                BigDecimal.ZERO, LocalDateTime.now().minusMinutes(30));
        createOrderItem(order7, augmentin, 1, new BigDecimal("42.00"));
        createOrderItem(order7, claritine, 1, new BigDecimal("19.99"));
        updateOrderTotal(order7);

        Order order8 = createOrder("ORD-20260610-0008", client2, catenaUnirii, OrderStatus.EXPIRED,
                BigDecimal.ZERO, LocalDateTime.now().minusDays(5));
        order8.setExpiresAt(LocalDateTime.now().minusDays(4));
        createOrderItem(order8, omeprazol, 2, new BigDecimal("22.00"));
        updateOrderTotal(order8);
        orderRepository.save(order8);

        System.out.println("✅ Seed data loaded successfully — Phase 2 enhanced dataset.");
    }

    // ── Helper methods ─────────────────────────────────────────────

    private Medication createMed(String ean, String name, String activeSubstance, MedForm form,
                                  String dosage, String category, boolean prescriptionRequired) {
        return Medication.builder()
                .ean(ean)
                .name(name)
                .activeSubstance(activeSubstance)
                .form(form)
                .dosage(dosage)
                .category(category)
                .prescriptionRequired(prescriptionRequired)
                .build();
    }

    private void createStock(Location location, Medication medication, int quantity, String price) {
        MedStock stock = MedStock.builder()
                .location(location)
                .medication(medication)
                .quantity(quantity)
                .price(new BigDecimal(price))
                .build();
        medStockRepository.save(stock);
    }

    private Order createOrder(String orderNumber, Client client, Location pickupLocation,
                              OrderStatus status, BigDecimal totalPrice, LocalDateTime createdAt) {
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .client(client)
                .pickupLocation(pickupLocation)
                .status(status)
                .totalPrice(totalPrice)
                .build();
        order.setCreatedAt(createdAt);
        return orderRepository.save(order);
    }

    private void createOrderItem(Order order, Medication medication, int quantity, BigDecimal unitPrice) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .medication(medication)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
        orderItemRepository.save(item);
    }

    private void updateOrderTotal(Order order) {
        // Re-fetch items after creating them
        BigDecimal total = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder().getId().equals(order.getId()))
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);
        orderRepository.save(order);
    }

    private void createDefaultWorkingHours(Location location) {
        for (DayOfWeek day : DayOfWeek.values()) {
            boolean isSunday = day == DayOfWeek.SUNDAY;
            boolean isSaturday = day == DayOfWeek.SATURDAY;

            WorkingHour wh = WorkingHour.builder()
                    .location(location)
                    .dayOfWeek(day)
                    .openTime(isSunday ? null : (isSaturday ? LocalTime.of(9, 0) : LocalTime.of(8, 0)))
                    .closeTime(isSunday ? null : (isSaturday ? LocalTime.of(14, 0) : LocalTime.of(20, 0)))
                    .closed(isSunday)
                    .build();
            workingHourRepository.save(wh);
        }
    }
}
