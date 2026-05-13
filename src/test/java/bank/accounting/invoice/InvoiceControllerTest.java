package bank.accounting.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import bank.accounting.invoice.calculator.InvoiceCalculator;
import bank.accounting.invoice.controller.InvoiceController;
import bank.accounting.invoice.dto.InvoiceRequest;
import bank.accounting.invoice.dto.InvoiceResponse;
import bank.accounting.invoice.dto.PaymentRequest;
import bank.accounting.invoice.exception.GlobalExceptionHandler;
import bank.accounting.invoice.model.InvoiceLine;
import bank.accounting.invoice.repository.InMemoryInvoiceRepository;
import bank.accounting.invoice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvoiceControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private InvoiceService invoiceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(new InMemoryInvoiceRepository(), new InvoiceCalculator());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new InvoiceController(invoiceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createInvoiceReturnsCalculatedDraftInvoice() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request("Norea Accounting", LocalDate.of(2026, 5, 13)))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/v1/invoices/")))
                .andExpect(jsonPath("$.invoiceId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.customerName").value("Norea Accounting"))
                .andExpect(jsonPath("$.subtotalWithoutVat").value(100.00))
                .andExpect(jsonPath("$.totalVat").value(24.00))
                .andExpect(jsonPath("$.finalTotal").value(124.00))
                .andExpect(jsonPath("$.paidAmount").value(0.00))
                .andExpect(jsonPath("$.remainingAmount").value(124.00));
    }

    @Test
    void createInvoiceRejectsInvalidRequestBody() throws Exception {
        InvoiceRequest invalidRequest = new InvoiceRequest(
                "",
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 27),
                List.of()
        );

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void getInvoicesReturnsFilteredInvoices() throws Exception {
        invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 13)));
        invoiceService.createInvoice(request("ACME Ltd", LocalDate.of(2026, 5, 14)));

        mockMvc.perform(get("/api/v1/invoices")
                        .param("status", "DRAFT")
                        .param("customerName", "norea")
                        .param("fromDate", "2026-05-13")
                        .param("toDate", "2026-05-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerName").value("Norea Accounting"));
    }

    @Test
    void getInvoicesRejectsInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/invoices")
                        .param("fromDate", "2026-05-14")
                        .param("toDate", "2026-05-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("From date cannot be after to date"));
    }

    @Test
    void registerPaymentReturnsUpdatedInvoice() throws Exception {
        InvoiceResponse invoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 13)));

        mockMvc.perform(post("/api/v1/invoices/{id}/payments", invoice.invoiceId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new PaymentRequest(new BigDecimal("50.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.paidAmount").value(50.00))
                .andExpect(jsonPath("$.remainingAmount").value(74.00));
    }

    @Test
    void registerPaymentRejectsInvalidAmount() throws Exception {
        InvoiceResponse invoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 13)));

        mockMvc.perform(post("/api/v1/invoices/{id}/payments", invoice.invoiceId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new PaymentRequest(BigDecimal.ZERO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void getInvoiceReturnsNotFoundForUnknownId() throws Exception {
        UUID invoiceId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Invoice not found: " + invoiceId));
    }

    @Test
    void registerPaymentReturnsBadRequestForOverpayment() throws Exception {
        InvoiceResponse invoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 13)));

        mockMvc.perform(post("/api/v1/invoices/{id}/payments", invoice.invoiceId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new PaymentRequest(new BigDecimal("124.01")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Payment amount exceeds remaining invoice amount"));
    }

    @Test
    void registerPaymentReturnsConflictForAlreadyPaidInvoice() throws Exception {
        InvoiceResponse invoice = invoiceService.createInvoice(request("Norea Accounting", LocalDate.of(2026, 5, 13)));
        invoiceService.registerPayment(invoice.invoiceId(), new PaymentRequest(new BigDecimal("124.00")));

        mockMvc.perform(post("/api/v1/invoices/{id}/payments", invoice.invoiceId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new PaymentRequest(new BigDecimal("1.00")))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invoice is already paid"));
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
