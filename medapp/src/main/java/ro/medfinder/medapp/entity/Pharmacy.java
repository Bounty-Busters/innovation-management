package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entitate lanț de farmacii (ex: "Farmacia Tei", "Dr. Max").
 * CUI-ul este pe Pharmacy, nu pe PharmOwner.
 */
@Entity
@Table(name = "pharmacies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"owner", "locations", "syncLogs"})
@EqualsAndHashCode(callSuper = true, exclude = {"owner", "locations", "syncLogs"})
public class Pharmacy extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "cui", unique = true, length = 20)
    private String cui;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sync_enabled", nullable = false)
    @Builder.Default
    private Boolean syncEnabled = false;

    @Column(name = "sync_endpoint_url", length = 500)
    private String syncEndpointUrl;

    // ── Relații ──────────────────────────────────────────────────

    /** Proprietarul farmaciei (PharmOwner). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    /** Punctele de lucru ale farmaciei. */
    @OneToMany(mappedBy = "pharmacy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Location> locations = new ArrayList<>();

    /** Istoricul sincronizărilor CSV/ERP. */
    @OneToMany(mappedBy = "pharmacy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SyncLog> syncLogs = new ArrayList<>();
}
