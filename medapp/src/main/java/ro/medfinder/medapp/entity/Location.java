package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Punct de lucru al unei farmacii (sucursală).
 * Stocul și farmaciștii sunt legați de Location, nu de Pharmacy.
 * Autorizare per punct de lucru.
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"pharmacy", "workingHours", "pharmacists", "medStocks", "orders"})
@EqualsAndHashCode(callSuper = true, exclude = {"pharmacy", "workingHours", "pharmacists", "medStocks", "orders"})
public class Location extends BaseEntity {

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "county", nullable = false, length = 100)
    private String county;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ── Relații ──────────────────────────────────────────────────

    /** Farmacia-mamă (lanțul). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    /** Programul de lucru pe zile. */
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkingHour> workingHours = new ArrayList<>();

    /** Farmaciștii care lucrează la acest punct de lucru. */
    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Pharmacist> pharmacists = new ArrayList<>();

    /** Stocurile de medicamente disponibile. */
    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedStock> medStocks = new ArrayList<>();

    /** Comenzile Click & Collect de ridicat de la acest punct. */
    @OneToMany(mappedBy = "pickupLocation", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
