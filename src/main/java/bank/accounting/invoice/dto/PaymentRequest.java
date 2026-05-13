package bank.accounting.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request payload for registering a payment against an existing invoice.
 */
public record PaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
