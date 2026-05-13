package bank.accounting.invoice.service;

import bank.accounting.invoice.calculator.InvoiceCalculator;
import bank.accounting.invoice.calculator.InvoiceTotals;
import bank.accounting.invoice.dto.InvoiceRequest;
import bank.accounting.invoice.dto.InvoiceResponse;
import bank.accounting.invoice.dto.PaymentRequest;
import bank.accounting.invoice.model.Invoice;
import bank.accounting.invoice.model.InvoiceStatus;
import bank.accounting.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Coordinates invoice business operations.
 *
 * <p>The service owns cross-field validation, invoice creation defaults,
 * filtering rules, payment state transitions, and conversion from domain model
 * to API response DTOs.</p>
 */
@Service
public class InvoiceService {

    private static final int MONEY_SCALE = 2;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceCalculator invoiceCalculator;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceCalculator invoiceCalculator) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceCalculator = invoiceCalculator;
    }

    /**
     * Creates a draft invoice from a validated request.
     *
     * <p>The invoice starts with no paid amount, the full final total as the
     * remaining amount, and {@link InvoiceStatus#DRAFT} as its initial status.</p>
     *
     * @param request invoice creation request
     * @return created invoice response
     * @throws IllegalArgumentException when required request fields or date rules are invalid
     */
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        validateRequest(request);

        InvoiceTotals totals = invoiceCalculator.calculate(request.invoiceLines());
        Invoice invoice = new Invoice(
                UUID.randomUUID(),
                totals.subtotalWithoutVat(),
                totals.totalVat(),
                totals.discountAmount(),
                totals.finalTotal(),
                BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                totals.finalTotal(),
                InvoiceStatus.DRAFT,
                request.customerName(),
                request.invoiceDate());

        return toResponse(invoiceRepository.save(invoice));
    }

    /**
     * Applies business validation that cannot be fully expressed by field-level
     * bean validation annotations.
     */
    private void validateRequest(InvoiceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Invoice request is required");
        }
        if (request.invoiceLines() == null || request.invoiceLines().isEmpty()) {
            throw new IllegalArgumentException("At least one invoice line is required");
        }
        if (request.invoiceDate() == null) {
            throw new IllegalArgumentException("Invoice date is required");
        }
        if (request.dueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        if (request.dueDate().isBefore(request.invoiceDate())) {
            throw new IllegalArgumentException("Due date cannot be before invoice date");
        }
    }

    /**
     * Loads a single invoice by id.
     *
     * @param invoiceId invoice identifier
     * @return matching invoice response
     * @throws NoSuchElementException when the invoice does not exist
     */
    public InvoiceResponse getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));
    }

    /**
     * Searches invoices using optional filters.
     *
     * <p>Filters are combined with logical AND. Customer name matching is
     * case-insensitive and partial; date bounds are inclusive.</p>
     *
     * @param status optional invoice status
     * @param customerName optional partial customer name
     * @param fromDate optional inclusive start date
     * @param toDate optional inclusive end date
     * @return matching invoice responses
     * @throws IllegalArgumentException when {@code fromDate} is after {@code toDate}
     */
    public List<InvoiceResponse> findInvoices(InvoiceStatus status,
                                              String customerName,
                                              LocalDate fromDate,
                                              LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }

        return invoiceRepository.findAll().stream()
                .filter(invoice -> status == null || invoice.status() == status)
                .filter(invoice -> customerName == null
                        || invoice.customerName().toLowerCase().contains(customerName.toLowerCase()))
                .filter(invoice -> fromDate == null
                        || !invoice.invoiceDate().isBefore(fromDate))
                .filter(invoice -> toDate == null
                        || !invoice.invoiceDate().isAfter(toDate))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Registers a payment against an existing invoice.
     *
     * <p>The method rejects payments for already paid invoices and payments that
     * would exceed the invoice's remaining balance. Valid payments are delegated
     * to the domain model so status calculation stays in one place.</p>
     *
     * @param invoiceId invoice receiving the payment
     * @param request payment request
     * @return updated invoice response
     * @throws NoSuchElementException when the invoice does not exist
     * @throws IllegalArgumentException when the payment is invalid or too large
     * @throws IllegalStateException when the invoice is already paid
     */
    public InvoiceResponse registerPayment(UUID invoiceId, PaymentRequest request) {
        validatePaymentRequest(request);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));

        if (invoice.isPaid()) {
            throw new IllegalStateException("Invoice is already paid");
        }

        if (invoice.wouldOverpay(request.amount())) {
            throw new IllegalArgumentException("Payment amount exceeds remaining invoice amount");
        }

        Invoice updatedInvoice = invoice.applyPayment(request.amount());
        return toResponse(invoiceRepository.save(updatedInvoice));
    }

    /**
     * Converts the internal domain model to the API response shape.
     */
    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.invoiceId(),
                invoice.subtotalWithoutVat(),
                invoice.totalVat(),
                invoice.discountAmount(),
                invoice.finalTotal(),
                invoice.paidAmount(),
                invoice.remainingAmount(),
                invoice.status(),
                invoice.customerName(),
                invoice.invoiceDate()
        );
    }

    /**
     * Validates payment input before invoice state rules are evaluated.
     */
    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

}
