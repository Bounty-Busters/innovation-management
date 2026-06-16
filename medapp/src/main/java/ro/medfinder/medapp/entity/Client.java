package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclasă de User — clientul care caută medicamente și plasează comenzi.
 * Câmpuri extra: deliveryAddress, city (pentru proximity search).
 */
@Entity
@DiscriminatorValue("CLIENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"orders"})
@EqualsAndHashCode(callSuper = true, exclude = {"orders"})
public class Client extends User {

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "city", length = 100)
    private String city;

    /** Comenzile plasate de acest client. */
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
