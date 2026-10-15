# Guardian — Payment Service

Microservice responsible for payments in an event-driven e-commerce system. It consumes `order.created` events published by the order-service and keeps its own database. This service is under development.

## Status

**Implemented**
- Consumer for the `order.created` topic (Kafka)
- `Payment` entity with validation in the constructor, `approve()` and `reject(reason)`
- `PaymentStatus` enum (PENDING, APPROVED, REJECTED) and `PaymentStatusTransitionValidator`
- Flyway migration that creates the `payments` table
- `PaymentRepository`

**Planned**
- Use case that creates and decides the payment from the received event
- Protection against duplicate events (one payment per order)
- Publishing `payment.approved` and `payment.rejected`
- Unit tests and CI

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Apache Kafka (Spring Kafka)
- Docker Compose
- Lombok
- Maven

## Architecture

The project follows the same layered architecture as the other services:

- **domain** → `Payment` entity, `PaymentStatus` and `PaymentStatusTransitionValidator`
- **application** → use cases (not implemented yet)
- **infrastructure** → JPA repository and Kafka (`OrderCreatedEvent`, `OrderCreatedConsumer`)
- **web** → reserved for controllers (no REST endpoints yet)

### Technical Decisions

- **Own `PaymentStatus` enum** — payment status is separate from order status; each service owns its own state
- **Own copy of `OrderCreatedEvent`** — no shared code with order-service; only the JSON contract connects them
- **Database per service** — payment-service owns its PostgreSQL database

See `DECISIONS.md` for the reasoning behind each one.

## Configuration

| Property | Description | Default |
|---|---|---|
| `DB_URL` | Database URL | `jdbc:postgresql://localhost:5435/guardian_payment` |
| `DB_USERNAME` | Database username | `guardian` |
| `DB_PASSWORD` | Database password | `guardian` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9092` |

## How to Run

### Prerequisites
- Docker
- Java 17
- Maven

### Starting the infrastructure

The `docker-compose.yml` lives in the catalog-service repository. From that folder:

```bash
docker-compose up -d
```

This starts the PostgreSQL instances (catalog, order and payment) and the Kafka broker.

### Running the application

```bash
mvn spring-boot:run
```

The application runs on port `8083`. To see it working, create an order through the order-service; the payment-service logs a `Received order.created` line for each event.

## Communication

This service consumes events from **Apache Kafka**:

- `order.created` — published by order-service when an order is created. It carries the order ID, the order total, the currency and a timestamp.

Publishing the payment result (`payment.approved` and `payment.rejected`) is planned.

## Testing

No tests yet. Planned: unit tests for the `Payment` entity and the transition validator.