package bank.accounting.invoice.calculator;

import bank.accounting.invoice.model.InvoiceLine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InvoiceCalculator {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 2;

    /**
     * Calculates the monetary totals for an invoice from its line items.
     *
     * <p>Each line contributes its full subtotal before VAT, then its discount is
     * subtracted before VAT is calculated. Final totals are rounded to two decimal
     * places using {@link RoundingMode#HALF_UP}, which keeps money values stable
     * for API responses and payment checks.</p>
     *
     * @param invoiceLines validated invoice lines to total
     * @return subtotal, VAT, discount, and final invoice total
     */
    public InvoiceTotals calculate(List<InvoiceLine> invoiceLines) {
        BigDecimal subtotalWithoutVat = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalVat = BigDecimal.ZERO;

        for (InvoiceLine line : invoiceLines) {
            BigDecimal lineSubtotal = line.unitPrice().multiply(BigDecimal.valueOf(line.quantity()));
            BigDecimal lineDiscount = percentageOf(lineSubtotal, line.discount());
            BigDecimal lineVat = percentageOf(lineSubtotal.subtract(lineDiscount), line.vatRate());

            subtotalWithoutVat = subtotalWithoutVat.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            totalVat = totalVat.add(lineVat);
        }

        subtotalWithoutVat = roundMoney(subtotalWithoutVat);
        discountAmount = roundMoney(discountAmount);
        totalVat = roundMoney(totalVat);
        BigDecimal finalTotal = roundMoney(subtotalWithoutVat.subtract(discountAmount).add(totalVat));

        return new InvoiceTotals(subtotalWithoutVat, totalVat, discountAmount, finalTotal);
    }

    /**
     * Calculates a percentage of a decimal money amount with currency rounding.
     */
    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage)
                .divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Normalizes money values to the API's two-decimal currency scale.
     */
    private BigDecimal roundMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
