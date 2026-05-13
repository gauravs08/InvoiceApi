package bank.accounting.invoice.repository;

import bank.accounting.invoice.model.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage boundary for invoices.
 *
 * <p>The service depends on this contract so the current in-memory storage can
 * be replaced by a database-backed implementation without changing business
 * logic or controllers.</p>
 */
public interface InvoiceRepository {

    /**
     * Stores a new or updated invoice snapshot.
     *
     * @param invoice invoice to persist
     * @return persisted invoice
     */
    Invoice save(Invoice invoice);

    /**
     * Finds one invoice by its unique id.
     *
     * @param invoiceId invoice identifier
     * @return invoice if present
     */
    Optional<Invoice> findById(UUID invoiceId);

    /**
     * Returns all known invoices for filtering in the service layer.
     *
     * @return snapshot list of invoices
     */
    List<Invoice> findAll();
}
