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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceServiceTest {

    private final InvoiceService invoiceService = new InvoiceService(new InMemoryInvoiceRepository(), new InvoiceCalculator());

    @Test
    void createInvoiceCalculatesTotalsAndDraftStatus() {
        InvoiceRequest request = new InvoiceRequest(
                "Norea Accounting",
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 26),
                List.of(
                        new InvoiceLine("Bookkeeping", 2, new BigDecimal("100.00"), new BigDecimal("24.00"), new BigDecimal("10.00")),
                        new InvoiceLine("Payroll", 1, new BigDecimal("50.00"), new BigDecimal("14.00"), BigDecimal.ZERO)
                )
        );

        InvoiceResponse response = invoiceService.createInvoice(request);

        assertThat(response.invoiceId()).isNotNull();
        assertThat(response.status()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(response.subtotalWithoutVat()).isEqualByComparingTo("250.00");
        assertThat(response.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(response.totalVat()).isEqualByComparingTo("50.20");
        assertThat(response.finalTotal()).isEqualByComparingTo("280.20");
    }

    @Test
    void createInvoiceRejectsEmptyInvoiceLines() {
        InvoiceRequest request = new InvoiceRequest(
                "Norea Accounting",
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 26),
                List.of()
        );

        assertThatThrownBy(() -> invoiceService.createInvoice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one invoice line is required");
    }

    @Test
    void createInvoiceRejectsDueDateBeforeInvoiceDate() {
        InvoiceRequest request = new InvoiceRequest(
                "Norea Accounting",
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 11),
                List.of(new InvoiceLine("Bookkeeping", 1, new BigDecimal("100.00"), new BigDecimal("24.00"), BigDecimal.ZERO))
        );

        assertThatThrownBy(() -> invoiceService.createInvoice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Due date cannot be before invoice date");
    }
}
