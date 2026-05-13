package bank.accounting.invoice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata shown in Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Registers API title, version, and description for generated documentation.
     */
    @Bean
    public OpenAPI invoiceApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Invoice API")
                        .version("1.0")
                        .description("Invoice creation, search, and payment API for the accounting mock project."));
    }
}
