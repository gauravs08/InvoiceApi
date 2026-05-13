# Invoice API

![Java 21](https://img.shields.io/badge/Java-21-blue)
![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![Maven](https://img.shields.io/badge/Build-Maven-orange)
![Tests](https://img.shields.io/badge/Tests-29%20passing-success)
![OpenAPI](https://img.shields.io/badge/API-OpenAPI-lightgrey)

Invoice API is a small Spring Boot microservice for creating invoices, searching invoices, and registering payments. It is designed as accounting API with clear layering, validation, predictable error responses, and test coverage.

The service uses in-memory storage for simplicity. In production, the repository interface can be replaced with a database-backed implementation without changing the controller API.

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Jakarta Validation
- Springdoc OpenAPI
- Maven
- JUnit 5 and MockMvc

## Project Structure

```text
bank.accounting.invoice
├── calculator   # Invoice total calculation
├── config       # OpenAPI configuration
├── controller   # REST endpoints
├── dto          # API request and response objects
├── exception    # Global API error handling
├── model        # Domain model and status enum
├── repository   # In-memory repository abstraction
└── service      # Business orchestration
```

## Run Locally

```bash
./mvnw spring-boot:run
```

The service starts on the default Spring Boot port:

```text
http://localhost:8080
```

## Run Tests

```bash
./mvnw test
```

Current test coverage includes invoice calculation, validation, filtering, not-found handling, payment state transitions, controller behavior, and API error responses.

## API Documentation

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## API Overview

### Create Invoice

```http
POST /api/v1/invoices
Content-Type: application/json
```

Request:

```json
{
  "customerName": "Norea Accounting",
  "invoiceDate": "2026-05-13",
  "dueDate": "2026-05-27",
  "invoiceLines": [
    {
      "description": "Bookkeeping",
      "quantity": 1,
      "unitPrice": 100.00,
      "vatRate": 24.00,
      "discount": 0.00
    }
  ]
}
```

Response:

```http
201 Created
Location: /api/v1/invoices/{invoiceId}
```

```json
{
  "invoiceId": "6d60d4ec-e78c-4ec4-9ef7-6ad0ad0183ef",
  "subtotalWithoutVat": 100.00,
  "totalVat": 24.00,
  "discountAmount": 0.00,
  "finalTotal": 124.00,
  "paidAmount": 0.00,
  "remainingAmount": 124.00,
  "status": "DRAFT",
  "customerName": "Norea Accounting",
  "invoiceDate": "2026-05-13"
}
```

### List Invoices

```http
GET /api/v1/invoices
```

Optional filters:

```http
GET /api/v1/invoices?status=DRAFT&customerName=norea&fromDate=2026-05-01&toDate=2026-05-31
```

Supported filters:

- `status`
- `customerName`
- `fromDate`
- `toDate`

### Get Invoice By ID

```http
GET /api/v1/invoices/{id}
```

Returns one invoice or `404 Not Found` if the invoice does not exist.

### Register Payment

```http
POST /api/v1/invoices/{id}/payments
Content-Type: application/json
```

Request:

```json
{
  "amount": 50.00
}
```

Response:

```json
{
  "invoiceId": "6d60d4ec-e78c-4ec4-9ef7-6ad0ad0183ef",
  "subtotalWithoutVat": 100.00,
  "totalVat": 24.00,
  "discountAmount": 0.00,
  "finalTotal": 124.00,
  "paidAmount": 50.00,
  "remainingAmount": 74.00,
  "status": "PARTIALLY_PAID",
  "customerName": "Norea Accounting",
  "invoiceDate": "2026-05-13"
}
```

Payment rules:

- Amount must be positive.
- Overpayment is rejected.
- A paid invoice cannot accept another payment.
- Partial payment changes status to `PARTIALLY_PAID`.
- Full payment changes status to `PAID`.

## Error Response

All handled API errors use the same response shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Invoice not found: 6d60d4ec-e78c-4ec4-9ef7-6ad0ad0183ef",
  "timestamp": "2026-05-13T10:15:30.123"
}
```

Common statuses:

- `400 Bad Request` for validation and business rule errors.
- `404 Not Found` when an invoice id does not exist.
- `409 Conflict` when a paid invoice receives another payment.

## Design Notes

- `BigDecimal` is used for all money and percentage calculations.
- `InvoiceCalculator` owns invoice total calculation.
- `InvoiceService` coordinates validation, repository access, and business operations.
- `InvoiceRepository` hides storage details.
- `InMemoryInvoiceRepository` is intentionally simple and replaceable.
- `Invoice` is the internal domain model.
- DTO records are used for API requests and responses.
- `GlobalExceptionHandler` keeps error responses consistent.

## Production Improvements

The current implementation is suitable for a coding exercise or mock microservice. Production work would usually add:

- Database persistence with JPA or JDBC.
- Payment history as a separate resource.
- Authentication and authorization.
- Pagination and sorting for invoice search.
- Currency handling.
- Audit fields such as created time and updated time.
- Observability through metrics, logs, and tracing.
- Container and deployment configuration.
