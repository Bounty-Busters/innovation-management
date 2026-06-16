package ro.medfinder.medapp.entity.enums;

/**
 * Stările posibile ale unei comenzi Click & Collect.
 *
 * Flux tipic: PENDING → ACCEPTED → READY_FOR_PICKUP → PICKED_UP
 * Excepții:   PENDING → REJECTED | CANCELLED
 *             ACCEPTED → CANCELLED
 *             READY_FOR_PICKUP → EXPIRED
 */
public enum OrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    READY_FOR_PICKUP,
    PICKED_UP,
    CANCELLED,
    EXPIRED
}
