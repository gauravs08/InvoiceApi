package bank.accounting.invoice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import bank.accounting.invoice.dto.InvoiceRequest;
import bank.accounting.invoice.dto.InvoiceResponse;
import bank.accounting.invoice.dto.PaymentRequest;
import bank.accounting.invoice.model.InvoiceStatus;
import bank.accounting.invoice.service.InvoiceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST entry point for invoice workflows.
 *
 * <p>The controller keeps HTTP concerns here and delegates business decisions to
 * {@link InvoiceService}.</p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Invoices", description = "Invoice creation, search, and payment operations")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Creates an invoice and returns a {@code 201 Created} response with the new
     * invoice URI in the {@code Location} header.
     */
    @PostMapping("/invoices")
    @Operation(summary = "Create an invoice")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest) {
        InvoiceResponse invoice = invoiceService.createInvoice(invoiceRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/invoices/" + invoice.invoiceId()))
                .body(invoice);
    }


    /**
     * Lists invoices, optionally narrowed by status, customer name, and inclusive
     * invoice date range.
     */
    @GetMapping("/invoices")
    @Operation(summary = "List invoices with optional filters")
    public List<InvoiceResponse> getInvoices(@RequestParam(required = false) InvoiceStatus status,
                                             @RequestParam(required = false) String customerName,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return invoiceService.findInvoices(status, customerName, fromDate, toDate);
    }


    /**
     * Returns one invoice or lets the global exception handler translate a missing
     * invoice into a {@code 404 Not Found} response.
     */
    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get an invoice by id")
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        return invoiceService.getInvoiceById(id);
    }

    /**
     * Registers a positive payment and returns the invoice with updated paid,
     * remaining, and status values.
     */
    @PostMapping("/invoices/{id}/payments")
    @Operation(summary = "Register a payment for an invoice")
    public InvoiceResponse registerPayment(@PathVariable UUID id,
                                           @Valid @RequestBody PaymentRequest paymentRequest) {
        return invoiceService.registerPayment(id, paymentRequest);
    }
}
