package ro.medfinder.medapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.medfinder.medapp.entity.Client;
import ro.medfinder.medapp.service.HoldingFeeCalculator.HoldingFeeResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HoldingFeeCalculatorTest {

    private HoldingFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HoldingFeeCalculator();
    }

    @Test
    void calculate_under2Hours_isFree() {
        Client client = new Client();
        client.setFreeLongReservationsLeft(0);

        HoldingFeeResult result1 = calculator.calculate(1, client);
        HoldingFeeResult result2 = calculator.calculate(2, client);

        assertEquals(0, BigDecimal.ZERO.compareTo(result1.fee()));
        assertFalse(result1.usedFreePerk());

        assertEquals(0, BigDecimal.ZERO.compareTo(result2.fee()));
        assertFalse(result2.usedFreePerk());
    }

    @Test
    void calculate_exact3HoursWithPerk_usesPerkAndIsFree() {
        Client client = new Client();
        client.setFreeLongReservationsLeft(1);

        HoldingFeeResult result = calculator.calculate(3, client);

        assertEquals(0, BigDecimal.ZERO.compareTo(result.fee()));
        assertTrue(result.usedFreePerk());
        assertEquals(0, client.getFreeLongReservationsLeft()); // Perk is consumed
    }

    @Test
    void calculate_exact3HoursWithoutPerk_isStandardFee() {
        Client client = new Client();
        client.setFreeLongReservationsLeft(0);

        HoldingFeeResult result = calculator.calculate(3, client);

        assertEquals(0, new BigDecimal("1.00").compareTo(result.fee()));
        assertFalse(result.usedFreePerk());
        assertEquals(0, client.getFreeLongReservationsLeft());
    }

    @Test
    void calculate_over3Hours_linearIncrease() {
        Client client = new Client();
        client.setFreeLongReservationsLeft(0);

        // 4 hours: 1 + (4-3)*(6/21) = 1.285... rounded to 0.25 -> 1.25
        HoldingFeeResult result4h = calculator.calculate(4, client);
        assertEquals(0, new BigDecimal("1.25").compareTo(result4h.fee()));
        assertFalse(result4h.usedFreePerk());

        // 24 hours: 1 + (24-3)*(6/21) = 1 + 21 * (6/21) = 7.00
        HoldingFeeResult result24h = calculator.calculate(24, client);
        assertEquals(0, new BigDecimal("7.00").compareTo(result24h.fee()));
        assertFalse(result24h.usedFreePerk());
    }
}
