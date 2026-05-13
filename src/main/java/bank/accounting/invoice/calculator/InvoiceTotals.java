package bank.accounting.invoice.calculator;

import java.math.BigDecimal;

/**
 * Immutable result of invoice total calculation.
 *
 * <p>The values are already rounded to the currency scale expected by the API.</p>
 */
public record InvoiceTotals(
        BigDecimal subtotalWithoutVat,
        BigDecimal totalVat,
        BigDecimal discountAmount,
        BigDecimal finalTotal
) {
}
