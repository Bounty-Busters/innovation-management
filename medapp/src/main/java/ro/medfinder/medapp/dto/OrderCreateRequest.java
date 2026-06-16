package ro.medfinder.medapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload trimis de frontend la crearea unei comenzi Click & Collect.
 */
@Getter
@Setter
public class OrderCreateRequest {

    @NotNull(message = "Medication ID is required")
    private Long medicationId;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Reservation hours is required")
    @Min(value = 1, message = "Reservation must be at least 1 hour")
    @Max(value = 24, message = "Reservation cannot exceed 24 hours")
    private Integer reservationHours;
}
