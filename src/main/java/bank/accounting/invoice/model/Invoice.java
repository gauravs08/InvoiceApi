package bank.accounting.invoice.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

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

    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean wouldOverpay(BigDecimal paymentAmount) {
        return paidAmount.add(paymentAmount).compareTo(finalTotal) > 0;
    }

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

    private BigDecimal roundMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
