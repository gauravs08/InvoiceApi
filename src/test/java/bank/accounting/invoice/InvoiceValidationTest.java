package bank.accounting.invoice;

import bank.accounting.invoice.dto.InvoiceRequest;
import bank.accounting.invoice.dto.PaymentRequest;
import bank.accounting.invoice.model.InvoiceLine;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void invoiceRequestRejectsBlankCustomerAndEmptyLines() {
        InvoiceRequest request = new InvoiceRequest(
                "",
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 27),
                List.of()
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("customerName", "invoiceLines");
    }

    @Test
    void invoiceRequestValidatesNestedInvoiceLine() {
        InvoiceRequest request = new InvoiceRequest(
                "Norea Accounting",
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 27),
                List.of(new InvoiceLine("", 0, BigDecimal.ZERO, new BigDecimal("101.00"), new BigDecimal("101.00")))
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "invoiceLines[0].description",
                        "invoiceLines[0].quantity",
                        "invoiceLines[0].unitPrice",
                        "invoiceLines[0].vatRate",
                        "invoiceLines[0].discount"
                );
    }

    @Test
    void paymentRequestRejectsZeroAmount() {
        PaymentRequest request = new PaymentRequest(BigDecimal.ZERO);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("amount");
    }
}
