package bank.accounting.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot launcher for the Invoice API application.
 */
@SpringBootApplication
public class InvoiceApiApplication {

    /**
     * Starts the embedded web server and application context.
     */
    public static void main(String[] args) {
        SpringApplication.run(InvoiceApiApplication.class, args);
    }

}
