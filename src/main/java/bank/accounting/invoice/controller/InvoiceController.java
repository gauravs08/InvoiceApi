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

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Invoices", description = "Invoice creation, search, and payment operations")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/invoices")
    @Operation(summary = "Create an invoice")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest) {
        InvoiceResponse invoice = invoiceService.createInvoice(invoiceRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/invoices/" + invoice.invoiceId()))
                .body(invoice);
    }


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


    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get an invoice by id")
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping("/invoices/{id}/payments")
    @Operation(summary = "Register a payment for an invoice")
    public InvoiceResponse registerPayment(@PathVariable UUID id,
                                           @Valid @RequestBody PaymentRequest paymentRequest) {
        return invoiceService.registerPayment(id, paymentRequest);
    }
}
