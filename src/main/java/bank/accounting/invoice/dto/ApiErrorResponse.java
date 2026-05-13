package bank.accounting.invoice.dto;

import java.time.LocalDateTime;

/**
 * Consistent error payload returned by the global exception handler.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
}
