# Product Domain Pricing

A reactive domain-layer microservice that orchestrates product pricing, fee management, and eligibility evaluation. Built on [FireflyFramework](https://github.com/fireflyframework/) and Spring WebFlux, this service exposes a CQRS/Saga-driven API that delegates persistence to the **core-common-product-mgmt** platform service.

> **Repository:** [https://github.com/firefly-oss/domain-product-pricing](https://github.com/firefly-oss/domain-product-pricing)

---

## Overview

Product Domain Pricing is the domain orchestration layer responsible for:

- **Pricing management** -- register and amend product pricing with tiered rates and effective-from date versioning.
- **Fee scheme management** -- define complete fee schemas (structure, components, application rules, and product-fee-structure linkage) within a single transactional saga with automatic compensation on failure.
- **Eligibility evaluation** -- publish, adjust, and evaluate eligibility criteria (KYC/KYB, credit score, income, activity) to determine applicant fit.
- **Scheme impact simulation** -- project the portfolio-wide impact of a proposed pricing scheme change (new interest rate / fee / APR) over all referencing products under a reference scenario. Read-only aggregation; no persistence.
- **Pricing waivers CRUD** -- create, update, remove and search pricing waivers / promotional discounts stored as product configurations with `configKey = WAIVER_<code>`.
- **Pricing history** -- best-effort audit timeline for a pricing entity, reconstructed from `ProductVersion` records and the configuration's own `createdAt`/`updatedAt` timestamps. Flagged as interim until a centralized audit store is available.
- **Event-driven architecture** -- every saga step emits domain events to Kafka for downstream consumers.
- **SDK generation** -- auto-generates a reactive Java client SDK from the OpenAPI specification.

> **Scope note.** An earlier iteration of this service exposed a `/rate-card/generate` endpoint that materialized PDF/XLSX rate-card files in-process using Apache POI and OpenHTMLToPDF. That feature was removed on tier-mismatch grounds (file rendering is a presentation / experience-tier concern, not a domain-orchestration concern; synchronous rendering blocks the WebFlux event loop; ~20 MB of transitive deps on a non-banking-core feature). Rate-card rendering should live in an experience service (e.g., `exp-backoffice-pricing`) that consumes this service's SDK plus the core `ProductConfigurationApi`, or in a dedicated document-generation service.

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

### Waivers (CQRS)

Pricing waivers are stored as `ProductConfiguration` entries on the parent product, with `configType = CUSTOM` and `configKey = WAIVER_<code>`. CRUD is exposed via a standard CQRS command/query pair:

| Component | Role |
|---|---|
| `CreateWaiverCommand` → `CreateWaiverHandler` | Serializes `WaiverSpec` to JSON, calls `productConfigurationApi.createConfiguration(productId, …, UUID.randomUUID().toString())`; fails fast if the response has no id. |
| `UpdateWaiverCommand` → `UpdateWaiverHandler` | Calls `productConfigurationApi.updateConfiguration(productId, waiverId, …)`. |
| `RemoveWaiverCommand` → `RemoveWaiverHandler` | Calls `productConfigurationApi.deleteConfiguration(productId, waiverId, …)`. |
| `WaiverSearchQuery`   → `SearchWaiversHandler` | Filters configurations by `WAIVER_` key prefix, deserializes the JSON blob to `WaiverSpec`, and applies in-memory filters (`codeContains`, `activeOn`). |

### Read-only services (no saga)

| Service | Purpose |
|---|---|
| `SchemeImpactSimulationService` | Projects the effect of a `PricingSchemeUpdate` over every pricing configuration referencing the scheme. For each affected product it computes current vs proposed monthly payment under a fixed reference scenario (€10 000 / 12 months) and returns per-product deltas plus aggregate `avg / maxIncrease / maxDecrease` percentages. Bounded concurrency via `Flux.flatMap(..., 8)`. |
| `PricingHistoryService` | Best-effort audit timeline for a pricing entity. Pulls `ProductVersion` records via `ProductVersionApi.filterProductVersions`, falls back to a single synthetic `CREATED` entry when none exist, swallows SDK errors and degrades gracefully. Flagged with a `TODO` comment until a centralized audit store is wired in. |

### Error handling: downstream transport failures

Because the waiver search goes through the CQRS `QueryBus`, a `WebClientRequestException` or `ConnectException` from the underlying `core-common-product-mgmt` call is wrapped in `QueryProcessingException` — which the framework's shared exception converter does not recognize, so the default response would be HTTP 500 `UNEXPECTED_ERROR`.

To align with the rest of the platform, `PricingServiceImpl.searchWaivers` walks the cause chain after the `QueryBus` wraps and re-maps transport failures to the framework's `ServiceUnavailableException`, which is rendered as HTTP 503 `service_unavailable` with an RFC 7807 body. Unrelated errors fall through untouched.

### Domain Events

All events are published to the `domain-layer` Kafka topic:

- `feeStructure.registered`
- `feeComponent.registered`
- `feeApplicationRule.registered`
- `productFeeStructure.registered`
- `fee.updated`
- `productPricing.registered`
- `pricing.updated`
- `pricing.waiver.created`
- `pricing.waiver.removed`
- `pricing.scheme-impact.simulated`

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

### Scheme impact + history

| Method | Endpoint                                              | Description                                        |
|--------|-------------------------------------------------------|----------------------------------------------------|
| POST   | `/api/v1/pricing/schemes/{id}/simulate-impact`        | Project the portfolio-wide impact of a proposed `PricingSchemeUpdate`. Read-only, returns `SchemeImpactReport` with per-product deltas and aggregate stats. |
| GET    | `/api/v1/pricing/history/{entityId}?from=…&to=…`      | Best-effort audit timeline for a pricing configuration entity. Optional `from` / `to` ISO-date range. |

### Waivers (`/api/v1/pricing/waivers`)

| Method | Endpoint                               | Description                                   |
|--------|----------------------------------------|-----------------------------------------------|
| POST   | `/api/v1/pricing/waivers`              | Create a waiver (body: `CreateWaiverCommand`). Returns `waiverId`. |
| PUT    | `/api/v1/pricing/waivers/{waiverId}?productId={uuid}`   | Replace a waiver's spec (body: `WaiverSpec`). |
| DELETE | `/api/v1/pricing/waivers/{waiverId}?productId={uuid}`   | Remove a waiver.                              |
| GET    | `/api/v1/pricing/waivers/search?productId=…&codeContains=…&activeOn=…` | Search waivers on a product with optional code substring + active-on date filters. |

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

Proprietary -- Firefly Software Solutions Inc.
