package bank.accounting.invoice.repository;

import bank.accounting.invoice.model.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(UUID invoiceId);

    List<Invoice> findAll();
}
