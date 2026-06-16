package ro.medfinder.medapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ro.medfinder.medapp.entity.enums.OrderStatus;

@Getter
@Setter
public class OrderStatusUpdate {

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;

    /** Required when newStatus is REJECTED. */
    private String rejectionReason;
}
