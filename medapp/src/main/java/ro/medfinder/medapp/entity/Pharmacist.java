package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Subclasă de User — farmacistul care lucrează la un punct de lucru (Location).
 * Nu are câmpuri suplimentare, doar relația către Location.
 * Prin {@code location.pharmacy} se poate naviga la farmacia-mamă.
 * Mutarea la altă locație = simplu update pe {@code location_id}.
 */
@Entity
@DiscriminatorValue("PHARMACIST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"location"})
@EqualsAndHashCode(callSuper = true, exclude = {"location"})
public class Pharmacist extends User {

    /** Locația (punctul de lucru) la care lucrează farmacistul. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
}
