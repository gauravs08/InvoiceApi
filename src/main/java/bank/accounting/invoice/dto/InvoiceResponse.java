package bank.accounting.invoice.dto;

import bank.accounting.invoice.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Public invoice representation returned by the REST API.
 *
 * <p>The response exposes calculated totals and payment state, but not the full
 * internal invoice line details.</p>
 */
public record InvoiceResponse(
        UUID invoiceId,
        BigDecimal subtotalWithoutVat,
        BigDecimal totalVat,
        BigDecimal discountAmount,
        BigDecimal finalTotal,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        String customerName,
        LocalDate invoiceDate
) {
}
