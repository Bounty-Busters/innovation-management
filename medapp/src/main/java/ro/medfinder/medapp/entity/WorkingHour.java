package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Program de lucru per zi al unei locații.
 * Câte un rând per zi a săptămânii — permite logica "farmacie deschisă acum".
 *
 * Constrângere: UNIQUE(location_id, day_of_week) — o singură intrare per zi per locație.
 */
@Entity
@Table(name = "working_hours", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"location_id", "day_of_week"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"location"})
@EqualsAndHashCode(callSuper = true, exclude = {"location"})
public class WorkingHour extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /** Ora deschidere (ex: 08:00). Nullable când {@code closed = true}. */
    @Column(name = "open_time")
    private LocalTime openTime;

    /** Ora închidere (ex: 20:00). Nullable când {@code closed = true}. */
    @Column(name = "close_time")
    private LocalTime closeTime;

    /** {@code true} = închis toată ziua (openTime/closeTime sunt ignorate). */
    @Column(name = "closed", nullable = false)
    @Builder.Default
    private Boolean closed = false;

    // ── Relații ──────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
}
