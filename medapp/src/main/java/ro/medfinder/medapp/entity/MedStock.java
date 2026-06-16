package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entitate de joncțiune Location ↔ Medication (Many-to-Many promovată).
 * Stochează stocul, prețul și data ultimei sincronizări per punct de lucru.
 *
 * Constrângere: UNIQUE(location_id, medication_id) — un medicament apare o singură dată per locație.
 */
@Entity
@Table(name = "med_stocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"location_id", "medication_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"location", "medication"})
@EqualsAndHashCode(callSuper = true, exclude = {"location", "medication"})
public class MedStock extends BaseEntity {

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    // ── Relații ──────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;
}
