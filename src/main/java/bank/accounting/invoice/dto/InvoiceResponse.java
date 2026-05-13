package bank.accounting.invoice.dto;

import bank.accounting.invoice.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
