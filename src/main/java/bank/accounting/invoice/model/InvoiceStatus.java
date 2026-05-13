package bank.accounting.invoice.model;

/**
 * Business state of an invoice across creation, approval, and payment.
 */
public enum InvoiceStatus {
    DRAFT,
    PENDING,
    APPROVED,
    PARTIALLY_PAID,
    PAID,
}
