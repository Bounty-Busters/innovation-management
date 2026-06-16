package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.medfinder.medapp.entity.Client;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculează taxa de rezervare (holding fee) pentru Click & Collect.
 *
 * Reguli:
 * - ≤ 2h: gratuit (0 RON)
 * - 3h–24h: creștere liniară de la 1 RON la 7 RON
 *   Formula: 1 + (ore - 3) * (6 / 21), rotunjit la cel mai apropiat 0.25
 * - Perk gratuit: dacă 3–5h și client.freeLongReservationsLeft > 0,
 *   se consumă o gratuitate și taxa = 0
 */
@Service
@RequiredArgsConstructor
public class HoldingFeeCalculator {

    /**
     * Rezultatul calculului: taxa efectivă + flag dacă s-a consumat o gratuitate.
     */
    public record HoldingFeeResult(BigDecimal fee, boolean usedFreePerk) {}

    /**
     * Calculează holding fee-ul pe baza orelor de rezervare și perk-urilor clientului.
     * <p>
     * ATENȚIE: Dacă se consumă un perk, metoda decrementează
     * {@code client.freeLongReservationsLeft} in-memory. Apelantul este responsabil
     * să persiste clientul în cadrul tranzacției.
     *
     * @param hours  numărul de ore solicitat (1–24)
     * @param client clientul care plasează comanda
     * @return HoldingFeeResult cu taxa și flag-ul de perk utilizat
     */
    public HoldingFeeResult calculate(int hours, Client client) {
        // ≤ 2h: gratuit
        if (hours <= 2) {
            return new HoldingFeeResult(BigDecimal.ZERO, false);
        }

        // 3–5h cu perk gratuit disponibil
        if (hours <= 5 && client.getFreeLongReservationsLeft() != null
                && client.getFreeLongReservationsLeft() > 0) {
            client.setFreeLongReservationsLeft(client.getFreeLongReservationsLeft() - 1);
            return new HoldingFeeResult(BigDecimal.ZERO, true);
        }

        // 3h–24h: formulă liniară
        double rawPrice = 1.0 + (hours - 3) * (6.0 / 21.0);
        double rounded = Math.round(rawPrice * 4) / 4.0;
        BigDecimal fee = BigDecimal.valueOf(rounded).setScale(2, RoundingMode.HALF_UP);
        return new HoldingFeeResult(fee, false);
    }
}
