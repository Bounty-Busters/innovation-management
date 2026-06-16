package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclasă de User — proprietarul unui lanț de farmacii.
 * CUI-ul se pune doar pe entitatea {@link Pharmacy}, nu pe PharmOwner.
 */
@Entity
@DiscriminatorValue("PHARM_OWNER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"pharmacies"})
@EqualsAndHashCode(callSuper = true, exclude = {"pharmacies"})
public class PharmOwner extends User {

    @Column(name = "company_name", length = 200)
    private String companyName;

    /** Farmaciile deținute de acest proprietar. */
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Pharmacy> pharmacies = new ArrayList<>();
}
