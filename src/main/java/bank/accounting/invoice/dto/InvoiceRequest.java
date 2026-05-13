package bank.accounting.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import bank.accounting.invoice.model.InvoiceLine;

import java.time.LocalDate;
import java.util.List;

public record InvoiceRequest(
        @NotBlank String customerName,
        @NotNull LocalDate invoiceDate,
        @NotNull LocalDate dueDate,
        @NotEmpty List<@Valid InvoiceLine> invoiceLines
) {}
