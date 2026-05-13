package bank.accounting.invoice;

import bank.accounting.invoice.dto.InvoiceRequest;
import bank.accounting.invoice.dto.InvoiceResponse;
import bank.accounting.invoice.calculator.InvoiceCalculator;
import bank.accounting.invoice.model.InvoiceLine;
import bank.accounting.invoice.model.InvoiceStatus;
import bank.accounting.invoice.repository.InMemoryInvoiceRepository;
import bank.accounting.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceServiceTask3Test {

    private final InvoiceService invoiceService = new InvoiceService(new InMemoryInvoiceRepository(), new InvoiceCalculator());

    @Test
    void listInvoicesReturnsAllCreatedInvoices() {
        InvoiceResponse firstInvoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 12)));
        InvoiceResponse secondInvoice = invoiceService.createInvoice(request("ACME Ltd", LocalDate.of(2026, 5, 13)));

        List<InvoiceResponse> invoices = invoiceService.findInvoices(null, null, null, null);

        assertThat(invoices)
                .extracting(InvoiceResponse::invoiceId)
                .containsExactly(firstInvoice.invoiceId(), secondInvoice.invoiceId());
    }

    @Test
    void getInvoiceByIdReturnsMatchingInvoice() {
        InvoiceResponse createdInvoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 12)));

        InvoiceResponse foundInvoice = invoiceService.getInvoiceById(createdInvoice.invoiceId());

        assertThat(foundInvoice.invoiceId()).isEqualTo(createdInvoice.invoiceId());
        assertThat(foundInvoice.finalTotal()).isEqualByComparingTo(createdInvoice.finalTotal());
        assertThat(foundInvoice.status()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void getInvoiceByIdRejectsUnknownId() {
        UUID unknownInvoiceId = UUID.randomUUID();

        assertThatThrownBy(() -> invoiceService.getInvoiceById(unknownInvoiceId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Invoice not found: " + unknownInvoiceId);
    }

    @Test
    void findInvoicesFiltersByStatus() {
        InvoiceResponse createdInvoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 12)));

        List<InvoiceResponse> draftInvoices = invoiceService.findInvoices(InvoiceStatus.DRAFT, null, null, null);
        List<InvoiceResponse> pendingInvoices = invoiceService.findInvoices(InvoiceStatus.PENDING, null, null, null);

        assertThat(draftInvoices)
                .extracting(InvoiceResponse::invoiceId)
                .containsExactly(createdInvoice.invoiceId());
        assertThat(pendingInvoices).isEmpty();
    }

    @Test
    void findInvoicesFiltersByCustomerNameIgnoringCase() {
        InvoiceResponse noreaInvoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 12)));
        invoiceService.createInvoice(request("ACME Ltd", LocalDate.of(2026, 5, 13)));

        List<InvoiceResponse> invoices = invoiceService.findInvoices(null, "norea", null, null);

        assertThat(invoices)
                .extracting(InvoiceResponse::invoiceId)
                .containsExactly(noreaInvoice.invoiceId());
    }

    @Test
    void findInvoicesFiltersByInvoiceDateRange() {
        invoiceService.createInvoice(request("Before Range", LocalDate.of(2026, 5, 9)));
        InvoiceResponse firstMatchingInvoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 10)));
        InvoiceResponse secondMatchingInvoice = invoiceService.createInvoice(request("ACME Ltd", LocalDate.of(2026, 5, 12)));
        invoiceService.createInvoice(request("After Range", LocalDate.of(2026, 5, 13)));

        List<InvoiceResponse> invoices = invoiceService.findInvoices(
                null,
                null,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12)
        );

        assertThat(invoices)
                .extracting(InvoiceResponse::invoiceId)
                .containsExactly(firstMatchingInvoice.invoiceId(), secondMatchingInvoice.invoiceId());
    }

    @Test
    void findInvoicesRejectsInvalidDateRange() {
        assertThatThrownBy(() -> invoiceService.findInvoices(
                null,
                null,
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 10)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From date cannot be after to date");
    }

    private InvoiceRequest request(String customerName, LocalDate invoiceDate) {
        return new InvoiceRequest(
                customerName,
                invoiceDate,
                invoiceDate.plusDays(14),
                List.of(new InvoiceLine("Bookkeeping", 1, new BigDecimal("100.00"), new BigDecimal("24.00"), BigDecimal.ZERO))
        );
    }
}
