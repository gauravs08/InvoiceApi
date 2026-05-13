package bank.accounting.invoice.repository;

import bank.accounting.invoice.model.Invoice;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<UUID, Invoice> invoices = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public Invoice save(Invoice invoice) {
        invoices.put(invoice.invoiceId(), invoice);
        return invoice;
    }

    @Override
    public Optional<Invoice> findById(UUID invoiceId) {
        return Optional.ofNullable(invoices.get(invoiceId));
    }

    @Override
    public List<Invoice> findAll() {
        synchronized (invoices) {
            return new ArrayList<>(invoices.values());
        }
    }
}
