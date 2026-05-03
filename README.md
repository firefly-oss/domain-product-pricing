# Product Domain Pricing

A reactive domain-layer microservice that orchestrates product pricing, fee management, and eligibility evaluation. Built on [FireflyFramework](https://github.com/fireflyframework/) and Spring WebFlux, this service exposes a CQRS/Saga-driven API that delegates persistence to the **core-common-product-mgmt** platform service.

> **Repository:** [https://github.com/firefly-oss/domain-product-pricing](https://github.com/firefly-oss/domain-product-pricing)

---

## Overview

Product Domain Pricing is the domain orchestration layer responsible for:

- **Pricing management** -- register and amend product pricing with tiered rates and effective-from date versioning.
- **Fee scheme management** -- define complete fee schemas (structure, components, application rules, and product-fee-structure linkage) within a single transactional saga with automatic compensation on failure.
- **Eligibility evaluation** -- publish, adjust, and evaluate eligibility criteria (KYC/KYB, credit score, income, activity) to determine applicant fit.
- **Event-driven architecture** -- every saga step emits domain events to Kafka for downstream consumers.
- **SDK generation** -- auto-generates a reactive Java client SDK from the OpenAPI specification.

---

## Architecture

### Module Structure

```
domain-product-pricing (parent POM)
|-- domain-product-pricing-core         # Domain logic: commands, handlers, services, sagas, constants
|-- domain-product-pricing-interfaces   # Interface/contract layer between core and web
|-- domain-product-pricing-infra        # Infrastructure: API client factory, configuration properties
|-- domain-product-pricing-web          # Spring Boot application, REST controllers, OpenAPI config
|-- domain-product-pricing-sdk          # Auto-generated reactive client SDK (OpenAPI Generator)
```

### Tech Stack

| Layer              | Technology                                                                                            |
|--------------------|-------------------------------------------------------------------------------------------------------|
| Language           | Java 25                                                                                               |
| Framework          | Spring Boot, Spring WebFlux (reactive)                                                                |
| Virtual Threads    | Enabled (`spring.threads.virtual.enabled: true`)                                                      |
| CQRS / Saga       | [FireflyFramework Transactional Saga Engine](https://github.com/fireflyframework/) with `CommandBus`  |
| Event Streaming    | Kafka (via FireflyFramework EDA publisher)                                                            |
| API Documentation  | SpringDoc OpenAPI (Swagger UI)                                                                        |
| Metrics            | Micrometer + Prometheus                                                                               |
| Mapping            | MapStruct + Lombok                                                                                    |
| SDK Generation     | OpenAPI Generator Maven Plugin (webclient / reactive)                                                 |
| Build              | Maven (multi-module)                                                                                  |
| BOM                | `fireflyframework-bom:26.01.01`                                                                       |

### Key Dependencies (FireflyFramework)

| Artifact                        | Purpose                                     |
|---------------------------------|---------------------------------------------|
| `fireflyframework-parent`       | Parent POM with managed dependency versions |
| `fireflyframework-bom`          | Bill of Materials for all framework modules  |
| `fireflyframework-web`          | Common web configuration and filters         |
| `fireflyframework-domain`       | Domain building blocks (Saga, CQRS)          |
| `fireflyframework-utils`        | Shared utility classes                       |
| `fireflyframework-validators`   | Reusable validation components               |

### Sagas (Workflow Orchestrations)

| Saga                     | Steps                                                                                                    | Compensation |
|--------------------------|----------------------------------------------------------------------------------------------------------|--------------|
| `RegisterFeeSchemaSaga`  | registerFeeStructure -> registerFeeComponent -> registerFeeApplicationRule -> registerProductFeeStructure | Full rollback via compensate methods |
| `UpdateFeeRuleSaga`      | updateFeeApplicationRule                                                                                 | None         |
| `RegisterPricingSaga`    | registerProductPricing                                                                                   | None         |
| `UpdatePricingSaga`      | updatePricing                                                                                            | None         |

### Domain Events

All events are published to the `domain-layer` Kafka topic:

- `feeStructure.registered`
- `feeComponent.registered`
- `feeApplicationRule.registered`
- `productFeeStructure.registered`
- `fee.updated`
- `productPricing.registered`
- `pricing.updated`

---

## Setup

### Prerequisites

- **Java 25** (JDK)
- **Apache Maven 3.9+**
- **Apache Kafka** (default: `localhost:9092`)
- **core-common-product-mgmt** service running (default: `http://localhost:8082`)

### Environment Variables

| Variable         | Default       | Description                            |
|------------------|---------------|----------------------------------------|
| `SERVER_ADDRESS` | `localhost`   | Server bind address                    |
| `SERVER_PORT`    | `8080`        | Server listening port                  |

### Application Configuration (application.yaml)

| Property                                                | Default              | Description                         |
|---------------------------------------------------------|----------------------|-------------------------------------|
| `firefly.cqrs.command.timeout`                          | `30s`                | Command execution timeout           |
| `firefly.cqrs.query.timeout`                            | `15s`                | Query execution timeout             |
| `firefly.cqrs.query.cache-ttl`                          | `15m`                | Query cache time-to-live            |
| `firefly.eda.publishers.kafka.default.bootstrap-servers`| `localhost:9092`     | Kafka bootstrap servers             |
| `firefly.eda.publishers.kafka.default.default-topic`    | `domain-layer`       | Default Kafka topic                 |
| `api-configuration.common-platform.product-mgmt.base-path` | `http://localhost:8082` | Product management service URL |

### Spring Profiles

| Profile   | Logging Behavior                             | Swagger UI |
|-----------|----------------------------------------------|------------|
| `dev`     | DEBUG for `com.firefly`, R2DBC, Flyway       | Enabled    |
| `testing` | DEBUG for `com.firefly`, INFO for R2DBC      | Enabled    |
| `prod`    | WARN for root, INFO for `com.firefly`        | Disabled   |

### Build

```bash
# Full build (all modules)
./mvnw clean install

# Build skipping tests
./mvnw clean install -DskipTests
```

### Run

```bash
# Run with default profile
./mvnw -pl domain-product-pricing-web spring-boot:run

# Run with dev profile
./mvnw -pl domain-product-pricing-web spring-boot:run -Dspring-boot.run.profiles=dev

# Run as JAR
java -jar domain-product-pricing-web/target/domain-product-pricing.jar
```

---

## API Endpoints

**Base path:** `/api/v1/pricing`

### Pricing

| Method | Endpoint                      | Description                                             |
|--------|-------------------------------|---------------------------------------------------------|
| POST   | `/api/v1/pricing`             | Register pricing (rates with tiers and effective date)  |
| PUT    | `/api/v1/pricing/{pricingId}` | Amend pricing (new effective version with updated rates/margins/tiers) |

### Fees

| Method | Endpoint                                                   | Description                          |
|--------|------------------------------------------------------------|--------------------------------------|
| POST   | `/api/v1/pricing/fees/schemes`                             | Define a fee scheme (structure, components, rules) |
| PUT    | `/api/v1/pricing/fees/schemes/{schemeId}/components/{componentId}` | Update a specific fee calculation rule |

### Eligibility

| Method | Endpoint                                          | Description                                        |
|--------|---------------------------------------------------|----------------------------------------------------|
| POST   | `/api/v1/pricing/eligibility`                     | Publish eligibility criteria (KYC/KYB, score, income, activity) |
| PATCH  | `/api/v1/pricing/eligibility/{eligibilityId}`     | Adjust eligibility criteria with versioning        |
| POST   | `/api/v1/pricing/eligibility/{eligibilityId}/evaluate` | Evaluate applicant facts (returns fit/not-fit with reasons) |

### OpenAPI / Swagger UI

- **API Docs (JSON):** `GET /v3/api-docs`
- **Swagger UI:** `GET /swagger-ui.html`

---

## Development Guidelines

### Project Conventions

- **Reactive programming** -- all service methods return `Mono<T>` or `Flux<T>`. Never block.
- **CQRS pattern** -- commands mutate state via `CommandBus`; queries read state via `QueryBus`.
- **Saga orchestration** -- multi-step workflows are defined as `@Saga` classes with `@SagaStep` methods and compensation handlers for rollback.
- **Step events** -- every saga step is annotated with `@StepEvent` to publish domain events automatically.
- **Immutable commands** -- use Lombok `@With` for creating modified copies (e.g., `command.withFeeStructureId(id)`).
- **Constants** -- all saga names, step IDs, compensation method names, and event types are defined in `ProductPricingConstants` and `GlobalConstants`.

### Module Responsibilities

- **core** -- pure domain logic; no Spring Web dependencies. Contains commands, handlers, services (interfaces and implementations), sagas/workflows, and constants.
- **interfaces** -- connects core to the outside world; depends on core.
- **infra** -- infrastructure concerns: API client factory for downstream service communication, configuration properties.
- **web** -- Spring Boot application entry point, REST controllers, OpenAPI definition. Depends on interfaces.
- **sdk** -- auto-generated client SDK from the OpenAPI spec. Consumers use this to call the service programmatically.

### Adding a New Domain Operation

1. Create a command class in `core/<subdomain>/commands/`.
2. Create a handler in `core/<subdomain>/handlers/`.
3. If multi-step, create or extend a saga in `core/<subdomain>/workflows/`.
4. Add constants (saga name, step ID, event type) to the appropriate constants class.
5. Wire through the service interface and implementation.
6. Expose via a controller endpoint in the `web` module.

---

## Monitoring

### Health and Readiness

| Endpoint                              | Description                |
|---------------------------------------|----------------------------|
| `GET /actuator/health`                | Overall application health |
| `GET /actuator/health/liveness`       | Kubernetes liveness probe  |
| `GET /actuator/health/readiness`      | Kubernetes readiness probe |

### Metrics

| Endpoint                     | Description                         |
|------------------------------|-------------------------------------|
| `GET /actuator/info`         | Application build information       |
| `GET /actuator/prometheus`   | Prometheus-format metrics scraping  |

CQRS command and query metrics are enabled via:
```yaml
firefly.cqrs.command.metrics-enabled: true
firefly.cqrs.command.tracing-enabled: true
```

---

## License

Proprietary -- Firefly Software Foundation.
