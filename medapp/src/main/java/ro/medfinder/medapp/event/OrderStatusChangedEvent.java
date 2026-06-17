package ro.medfinder.medapp.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ro.medfinder.medapp.entity.Order;

@Getter
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private final Order order;
}
