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
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
        // Chains (owner1)
        Pharmacy catena = createPharmacy("Catena", "RO12345678", "0700000001", "contact@catena.ro", "https://www.catena.ro", owner1);
        Pharmacy drMax = createPharmacy("Dr. Max", "RO87654321", "0700000002", "contact@drmax.ro", "https://www.drmax.ro", owner1);
        Pharmacy farmaTei = createPharmacy("Farmacia Tei", "RO11223344", "0700000003", "contact@farmaciatei.ro", "https://www.farmaciatei.ro", owner2);
        Pharmacy helpNet = createPharmacy("HelpNet", "RO55667788", "0700000004", "contact@helpnet.ro", "https://www.helpnet.ro", owner1);
        Pharmacy dona = createPharmacy("Dona", "RO99887766", "0700000005", "contact@farmaciiledona.ro", "https://www.farmaciiledona.ro", owner1);

        // Independents (owner2)
        Pharmacy sfAna = createPharmacy("Farmacia Sfânta Ana", "RO22334455", "0711000001", "sfanta.ana@independent.ro", null, owner2);
        Pharmacy hipocrat = createPharmacy("Farmacia Hipocrat", "RO33445566", "0711000002", "hipocrat@independent.ro", null, owner2);
        Pharmacy medikon = createPharmacy("Medikon Pharma", "RO44556677", "0711000003", "medikon@independent.ro", null, owner2);
        Pharmacy greenApoteca = createPharmacy("Green Apoteca", "RO55667711", "0711000004", "green.apoteca@independent.ro", null, owner2);

        // ── 4. LOCATIONS ───────────────────────────────────────────
        List<Location> locations = new ArrayList<>();

        // Bucharest locations
        Location catenaUnirii = createLoc("Catena Unirii", "Piata Unirii 1", "Bucuresti", "Bucuresti", "030031", 44.4268, 26.1025, "0210000001", catena);
        Location catenaVictoriei = createLoc("Catena Victoriei", "Calea Victoriei 100", "Bucuresti", "Bucuresti", "010072", 44.4400, 26.0960, "0210000002", catena);
        Location catenaObor = createLoc("Catena Obor", "Sos. Colentina 2", "Bucuresti", "Bucuresti", "021171", 44.4501, 26.1264, "0210000003", catena);
        Location drMaxRomana = createLoc("Dr. Max Romană", "Piata Romana 5", "Bucuresti", "Bucuresti", "010373", 44.4463, 26.0967, "0210000004", drMax);
        Location drMaxUniversitate = createLoc("Dr. Max Universitate", "Bulevardul Regina Elisabeta 3", "Bucuresti", "Bucuresti", "030011", 44.4356, 26.1025, "0210000005", drMax);
        Location farmaTeiFloreasca = createLoc("Farmacia Tei Floreasca", "Calea Floreasca 111-113", "Bucuresti", "Bucuresti", "014455", 44.4622, 26.1089, "0210000006", farmaTei);
        Location helpNetDorobanti = createLoc("HelpNet Dorobanți", "Calea Dorobanților 80", "Bucuresti", "Bucuresti", "010577", 44.4560, 26.0970, "0210000007", helpNet);
        Location donaDristor = createLoc("Dona Dristor", "Bulevardul Camil Ressu 1", "Bucuresti", "Bucuresti", "031731", 44.4208, 26.1415, "0210000008", dona);
        Location sfAnaBucuresti = createLoc("Farmacia Sfânta Ana", "Str. Lipscani 45", "Bucuresti", "Bucuresti", "030022", 44.4300, 26.1100, "0210000009", sfAna);
        Location hipocratBucuresti = createLoc("Farmacia Hipocrat Titan", "Str. Liviu Rebreanu 12", "Bucuresti", "Bucuresti", "031785", 44.4250, 26.1600, "0210000010", hipocrat);

        locations.addAll(List.of(catenaUnirii, catenaVictoriei, catenaObor, drMaxRomana, drMaxUniversitate, farmaTeiFloreasca, helpNetDorobanti, donaDristor, sfAnaBucuresti, hipocratBucuresti));

        // Cluj-Napoca locations (Centered around Dr. Max Cluj: 46.7712, 23.5912. Placed West-ward to be within 10km)
        Location drMaxCluj = createLoc("Dr. Max Cluj Centru", "Str. Memorandumului 10", "Cluj-Napoca", "Cluj", "400114", 46.7712, 23.5912, "0264000001", drMax);
        Location catenaClujGrigorescu = createLoc("Catena Cluj Grigorescu", "Str. Alexandru Vlahuță 2", "Cluj-Napoca", "Cluj", "400320", 46.7680, 23.5550, "0264000002", catena);
        Location helpNetClujManastur = createLoc("HelpNet Cluj Mănăștur", "Calea Florești 77", "Cluj-Napoca", "Cluj", "400511", 46.7560, 23.5620, "0264000003", helpNet);
        Location donaClujGrigorescu = createLoc("Dona Cluj Grigorescu", "Str. Fântânele 11", "Cluj-Napoca", "Cluj", "400325", 46.7720, 23.5480, "0264000004", dona);
        Location farmaTeiClujWest = createLoc("Farmacia Tei Cluj West", "Str. Primăverii 2", "Cluj-Napoca", "Cluj", "400532", 46.7600, 23.5350, "0264000005", farmaTei);
        Location sfAnaCluj = createLoc("Farmacia Sfânta Ana Cluj", "Str. Donath 15", "Cluj-Napoca", "Cluj", "400300", 46.7690, 23.5700, "0264000006", sfAna);
        Location hipocratClujZorilor = createLoc("Farmacia Hipocrat Cluj Zorilor", "Str. Observatorului 109", "Cluj-Napoca", "Cluj", "400438", 46.7510, 23.5850, "0264000007", hipocrat);
        Location medikonCluj = createLoc("Medikon Pharma Cluj", "Str. Grigore Alexandrescu 5", "Cluj-Napoca", "Cluj", "400540", 46.7750, 23.5450, "0264000008", medikon);
        Location greenApotecaCluj = createLoc("Green Apoteca Cluj Centru", "Str. George Coșbuc 1", "Cluj-Napoca", "Cluj", "400375", 46.7705, 23.5800, "0264000009", greenApoteca);

        locations.addAll(List.of(drMaxCluj, catenaClujGrigorescu, helpNetClujManastur, donaClujGrigorescu, farmaTeiClujWest, sfAnaCluj, hipocratClujZorilor, medikonCluj, greenApotecaCluj));

        // Timișoara locations
        Location farmaTeiTimisoara = createLoc("Farmacia Tei Timișoara", "Bd. Revoluției 5", "Timișoara", "Timiș", "300024", 45.7489, 21.2087, "0256000001", farmaTei);
        Location catenaTimisoaraCentru = createLoc("Catena Timișoara Centru", "Str. Alba Iulia 2", "Timișoara", "Timiș", "300077", 45.7535, 21.2250, "0256000002", catena);
        Location donaTimisoaraIosefin = createLoc("Dona Timișoara Iosefin", "Str. Gheorghe Doja 20", "Timișoara", "Timiș", "300192", 45.7440, 21.2070, "0256000003", dona);
        Location greenApotecaTimisoara = createLoc("Green Apoteca Timișoara", "Piața Bălcescu 3", "Timișoara", "Timiș", "300223", 45.7420, 21.2260, "0256000004", greenApoteca);

        locations.addAll(List.of(farmaTeiTimisoara, catenaTimisoaraCentru, donaTimisoaraIosefin, greenApotecaTimisoara));

        // Save all locations & working hours
        for (Location loc : locations) {
            locationRepository.save(loc);
            createDefaultWorkingHours(loc);
        }

        // ── 5. PHARMACISTS ─────────────────────────────────────────
        Pharmacist pharmacist1 = createPharmacist("farmacist@medfinder.ro", "pharma123", "Maria", "Farmacist", catenaUnirii);
        Pharmacist pharmacist2 = createPharmacist("farmacist2@medfinder.ro", "pharma123", "Andrei", "Popescu", catenaVictoriei);
        Pharmacist pharmacist3 = createPharmacist("farmacist3@medfinder.ro", "pharma123", "Ana", "Dumitrescu", drMaxCluj);
        Pharmacist pharmacist4 = createPharmacist("farmacist4@medfinder.ro", "pharma123", "Mihai", "Popa", farmaTeiTimisoara);
        
        userRepository.saveAll(List.of(pharmacist1, pharmacist2, pharmacist3, pharmacist4));

        // ── 6. CLIENTS ─────────────────────────────────────────────
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

        // ── 7. MEDICATIONS ─────────────────────────────────────────
        List<Medication> meds = new ArrayList<>();
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
        
        // New Medications for richer catalog
        Medication nospa = createMed("5941732000164", "No-Spa 40mg", "Drotaverinum", MedForm.TABLET, "40mg", "Antispastice", false);
        Medication aspenter = createMed("5941732000171", "Aspenter 75mg", "Acidum acetylsalicylicum", MedForm.TABLET, "75mg", "Cardiovasculare", false);
        Medication ketonal = createMed("5941732000188", "Ketonal 100mg", "Ketoprofenum", MedForm.TABLET, "100mg", "Antiinflamatoare", true);
        Medication linex = createMed("5941732000195", "Linex Forte", "Lactobacillus + Bifidobacterium", MedForm.CAPSULE, "Standard", "Probiotice", false);
        Medication smecta = createMed("5941732000201", "Smecta", "Diosmectitum", MedForm.OTHER, "3g", "Gastroenterologie", false);
        Medication olynth = createMed("5941732000218", "Olynth 0.1% Spray", "Xylometazolinum", MedForm.OTHER, "10ml", "ORL", false);
        Medication aerius = createMed("5941732000225", "Aerius 5mg", "Desloratadinum", MedForm.TABLET, "5mg", "Antialergice", false);
        Medication zyrtec = createMed("5941732000232", "Zyrtec 10mg", "Cetirizinum", MedForm.TABLET, "10mg", "Antialergice", false);
        Medication voltaren = createMed("5941732000249", "Voltaren Emulgel 2%", "Diclofenacum", MedForm.OTHER, "100g", "Antiinflamatoare", false);

        meds.addAll(List.of(paracetamol, nurofen, amoxicilina, augmentin, metformin, amlodipina, omeprazol,
                vitamina_c, strepsils, aspirin, brufen, colebil, spasmomen, claritine, coldrex,
                nospa, aspenter, ketonal, linex, smecta, olynth, aerius, zyrtec, voltaren));

        medicationRepository.saveAll(meds);

        // ── 8. MED STOCKS (Crossover Seeding) ──────────────────────
        Set<String> existingStocks = new HashSet<>();

        // Ensure order-dependent stocks are seeded first
        createStockWithCheck(catenaUnirii, paracetamol, 150, "9.50", existingStocks);
        createStockWithCheck(catenaUnirii, nurofen, 45, "25.99", existingStocks);
        createStockWithCheck(catenaUnirii, amoxicilina, 30, "18.50", existingStocks);
        createStockWithCheck(catenaUnirii, omeprazol, 80, "22.00", existingStocks);
        createStockWithCheck(catenaUnirii, vitamina_c, 200, "12.99", existingStocks);
        createStockWithCheck(catenaUnirii, aspirin, 120, "15.00", existingStocks);

        createStockWithCheck(catenaVictoriei, paracetamol, 90, "9.50", existingStocks);
        createStockWithCheck(catenaVictoriei, augmentin, 25, "42.00", existingStocks);
        createStockWithCheck(catenaVictoriei, metformin, 60, "28.50", existingStocks);
        createStockWithCheck(catenaVictoriei, claritine, 40, "19.99", existingStocks);

        createStockWithCheck(drMaxCluj, nurofen, 70, "24.99", existingStocks);
        createStockWithCheck(drMaxCluj, amoxicilina, 35, "17.50", existingStocks);
        createStockWithCheck(drMaxCluj, amlodipina, 50, "32.00", existingStocks);
        createStockWithCheck(drMaxCluj, strepsils, 100, "16.50", existingStocks);
        createStockWithCheck(drMaxCluj, coldrex, 85, "14.99", existingStocks);

        createStockWithCheck(farmaTeiTimisoara, paracetamol, 110, "8.99", existingStocks);
        createStockWithCheck(farmaTeiTimisoara, brufen, 55, "21.00", existingStocks);
        createStockWithCheck(farmaTeiTimisoara, colebil, 40, "35.00", existingStocks);
        createStockWithCheck(farmaTeiTimisoara, spasmomen, 30, "45.00", existingStocks);
        createStockWithCheck(farmaTeiTimisoara, vitamina_c, 150, "11.99", existingStocks);

        // Generate dynamic crossover stocks using a seeded Random for consistency
        Random rand = new Random(42);
        for (Location loc : locations) {
            for (Medication med : meds) {
                // High demand meds (Paracetamol, Nurofen, Coldrex, Vitamin C, Voltaren, Olynth) should be in almost all pharmacies
                boolean isHighDemand = List.of(paracetamol, nurofen, coldrex, vitamina_c, voltaren, olynth).contains(med);
                double probability = isHighDemand ? 0.90 : 0.45;

                if (rand.nextDouble() < probability) {
                    // Decide price based on base price ranges
                    double baseMinPrice = 8.00;
                    double baseMaxPrice = 50.00;

                    if (med == paracetamol) { baseMinPrice = 7.50; baseMaxPrice = 11.50; }
                    else if (med == nurofen) { baseMinPrice = 21.00; baseMaxPrice = 28.50; }
                    else if (med == coldrex) { baseMinPrice = 13.00; baseMaxPrice = 18.00; }
                    else if (med == vitamina_c) { baseMinPrice = 9.99; baseMaxPrice = 14.50; }
                    else if (med == voltaren) { baseMinPrice = 34.00; baseMaxPrice = 45.00; }
                    else if (med == augmentin) { baseMinPrice = 38.00; baseMaxPrice = 48.00; }
                    else if (med == ketonal) { baseMinPrice = 19.00; baseMaxPrice = 26.00; }
                    else if (med == linex) { baseMinPrice = 28.00; baseMaxPrice = 36.00; }

                    double rawPrice = baseMinPrice + (baseMaxPrice - baseMinPrice) * rand.nextDouble();
                    BigDecimal price = BigDecimal.valueOf(rawPrice).setScale(2, RoundingMode.HALF_UP);
                    int qty = 10 + rand.nextInt(190); // 10 to 200 units

                    createStockWithCheck(loc, med, qty, price.toString(), existingStocks);
                }
            }
        }

        // ── 9. ORDERS ─────────────────────────────────────────────
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

        System.out.println("✅ Seed data loaded successfully — Massive high-crossover dataset initialized.");
    }

    // ── Helper methods ─────────────────────────────────────────────

    private Pharmacy createPharmacy(String name, String cui, String phone, String email, String website, PharmOwner owner) {
        Pharmacy p = Pharmacy.builder()
                .name(name)
                .cui(cui)
                .phone(phone)
                .email(email)
                .website(website)
                .owner(owner)
                .active(true)
                .syncEnabled(true)
                .build();
        return pharmacyRepository.save(p);
    }

    private Location createLoc(String name, String address, String city, String county, String postalCode,
                               double lat, double lng, String phone, Pharmacy pharmacy) {
        return Location.builder()
                .name(name)
                .address(address)
                .city(city)
                .county(county)
                .postalCode(postalCode)
                .latitude(lat)
                .longitude(lng)
                .phone(phone)
                .pharmacy(pharmacy)
                .active(true)
                .build();
    }

    private Pharmacist createPharmacist(String email, String rawPassword, String firstName, String lastName, Location location) {
        return Pharmacist.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.PHARMACIST)
                .enabled(true)
                .location(location)
                .build();
    }

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

    private void createStockWithCheck(Location location, Medication medication, int quantity, String price, Set<String> existingKeys) {
        String key = location.getName() + "_" + medication.getEan();
        if (existingKeys.contains(key)) {
            return;
        }
        existingKeys.add(key);
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

