package bank.accounting.invoice.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * One billable invoice line supplied by the client.
 *
 * <p>VAT and discount are percentages from {@code 0.00} to {@code 100.00};
 * money values use {@link BigDecimal} to avoid floating-point errors.</p>
 */
public record InvoiceLine(
        @NotBlank String description,
        @NotNull @Positive Integer quantity,
        @NotNull @DecimalMin("0.01") BigDecimal unitPrice,
        @NotNull @DecimalMin("0.00") @DecimalMax("100") BigDecimal vatRate,
        @NotNull @DecimalMin("0.00") @DecimalMax("100") BigDecimal discount) {
}
