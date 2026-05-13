package bank.accounting.invoice;

import bank.accounting.invoice.dto.InvoiceRequest;
import bank.accounting.invoice.dto.InvoiceResponse;
import bank.accounting.invoice.dto.PaymentRequest;
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

class InvoiceServiceTask4Test {

    private final InvoiceService invoiceService = new InvoiceService(new InMemoryInvoiceRepository(), new InvoiceCalculator());

    @Test
    void registerPaymentMarksInvoicePartiallyPaid() {
        InvoiceResponse invoice = invoiceService.createInvoice(request());

        InvoiceResponse updatedInvoice = invoiceService.registerPayment(
                invoice.invoiceId(),
                new PaymentRequest(new BigDecimal("50.00"))
        );

        assertThat(updatedInvoice.status()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(updatedInvoice.paidAmount()).isEqualByComparingTo("50.00");
        assertThat(updatedInvoice.remainingAmount()).isEqualByComparingTo("74.00");
    }

    @Test
    void registerPaymentMarksInvoicePaidWhenFullAmountIsPaid() {
        InvoiceResponse invoice = invoiceService.createInvoice(request());

        InvoiceResponse updatedInvoice = invoiceService.registerPayment(
                invoice.invoiceId(),
                new PaymentRequest(new BigDecimal("124.00"))
        );

        assertThat(updatedInvoice.status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(updatedInvoice.paidAmount()).isEqualByComparingTo("124.00");
        assertThat(updatedInvoice.remainingAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void registerPaymentRejectsOverpayment() {
        InvoiceResponse invoice = invoiceService.createInvoice(request());

        assertThatThrownBy(() -> invoiceService.registerPayment(
                invoice.invoiceId(),
                new PaymentRequest(new BigDecimal("124.01"))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment amount exceeds remaining invoice amount");
    }

    @Test
    void registerPaymentRejectsUnknownInvoiceId() {
        UUID unknownInvoiceId = UUID.randomUUID();

        assertThatThrownBy(() -> invoiceService.registerPayment(
                unknownInvoiceId,
                new PaymentRequest(new BigDecimal("10.00"))
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Invoice not found: " + unknownInvoiceId);
    }

    @Test
    void registerPaymentRejectsAlreadyPaidInvoice() {
        InvoiceResponse invoice = invoiceService.createInvoice(request());
        invoiceService.registerPayment(invoice.invoiceId(), new PaymentRequest(new BigDecimal("124.00")));

        assertThatThrownBy(() -> invoiceService.registerPayment(
                invoice.invoiceId(),
                new PaymentRequest(new BigDecimal("1.00"))
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice is already paid");
    }

    @Test
    void registerPaymentRejectsZeroOrNegativePaymentAmount() {
        InvoiceResponse invoice = invoiceService.createInvoice(request());

        assertThatThrownBy(() -> invoiceService.registerPayment(
                invoice.invoiceId(),
                new PaymentRequest(BigDecimal.ZERO)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment amount must be positive");
    }

    private InvoiceRequest request() {
        return new InvoiceRequest(
                "Norea Accounting",
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 27),
                List.of(new InvoiceLine("Bookkeeping", 1, new BigDecimal("100.00"), new BigDecimal("24.00"), BigDecimal.ZERO))
        );
    }
}
