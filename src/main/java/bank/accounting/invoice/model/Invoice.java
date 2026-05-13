package bank.accounting.invoice.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Internal immutable invoice domain model.
 *
 * <p>Payment operations return a new {@code Invoice} instance instead of
 * mutating the existing one. This keeps state transitions explicit and makes
 * repository replacement easier later.</p>
 */
public record Invoice(
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
    private static final int MONEY_SCALE = 2;

    /**
     * @return {@code true} when no additional payments should be accepted
     */
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    /**
     * Checks whether accepting the supplied payment would push the paid amount
     * beyond the final invoice total.
     *
     * @param paymentAmount payment amount being registered
     * @return {@code true} if the payment would overpay the invoice
     */
    public boolean wouldOverpay(BigDecimal paymentAmount) {
        return paidAmount.add(paymentAmount).compareTo(finalTotal) > 0;
    }

    /**
     * Applies a validated payment and derives the next invoice status.
     *
     * <p>A payment that clears the remaining balance marks the invoice as
     * {@link InvoiceStatus#PAID}; otherwise the invoice becomes
     * {@link InvoiceStatus#PARTIALLY_PAID}.</p>
     *
     * @param paymentAmount validated positive payment amount
     * @return a new invoice snapshot with updated payment fields
     */
    public Invoice applyPayment(BigDecimal paymentAmount) {
        BigDecimal newPaidAmount = roundMoney(paidAmount.add(paymentAmount));
        BigDecimal newRemainingAmount = roundMoney(finalTotal.subtract(newPaidAmount));
        InvoiceStatus newStatus = newRemainingAmount.compareTo(BigDecimal.ZERO) == 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID;

        return new Invoice(
                invoiceId,
                subtotalWithoutVat,
                totalVat,
                discountAmount,
                finalTotal,
                newPaidAmount,
                newRemainingAmount,
                newStatus,
                customerName,
                invoiceDate
        );
    }

    /**
     * Keeps payment-derived money values on the same currency scale as totals.
     */
    private BigDecimal roundMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
