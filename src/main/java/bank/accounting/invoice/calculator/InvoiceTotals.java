package bank.accounting.invoice.calculator;

import java.math.BigDecimal;

public record InvoiceTotals(
        BigDecimal subtotalWithoutVat,
        BigDecimal totalVat,
        BigDecimal discountAmount,
        BigDecimal finalTotal
) {
}
