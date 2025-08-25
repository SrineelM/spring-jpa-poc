
# Production-Ready Spring Boot 3 Showcase

This project is a comprehensive showcase of a production-ready application built with Spring Boot 3. It demonstrates a wide array of advanced features, from core data persistence to enterprise-grade concerns like transaction management, resilience, observability, security, and concurrency.

## Key Architectural Patterns

-   **Advanced Data Persistence**: Solutions for the N+1 query problem (`@EntityGraph`, `JOIN FETCH`), efficient DTO projections, and dynamic, paginated searching.
-   **Advanced Transaction Management**: Declarative (`@Transactional`), programmatic (`TransactionTemplate`), and centralized AOP-based transaction strategies.
-   **Concurrency & Event-Driven Architecture**: A custom-configured thread pool for asynchronous tasks and a decoupled, event-driven workflow for user registration.
-   **Resilience (Resilience4j)**: Robust handling of external service failures using Circuit Breaker, Retry, Timeout, Bulkhead, and Rate Limiter patterns.
-   **Observability**: Deep insights into the application's behavior through structured logging, global error handling, and distributed tracing with Micrometer and Zipkin.
-   **Security (Spring Security & JWT)**: Secure, stateless authentication and authorization.
-   **Containerization**: A multi-stage `Dockerfile` for building a lightweight, production-ready container.

---

## Feature Details

### 1. Data Persistence & Performance

-   **N+1 Query Problem Solutions**: The `UserRepository` demonstrates how to solve the N+1 problem using both `@EntityGraph` and `JOIN FETCH` to eagerly load lazy associations in a single query.
-   **Efficient DTO Projections**: The `findAllUserSummaries()` method in `UserRepository` uses a JPQL constructor expression (`SELECT new ...`) to fetch data directly into a `UserSummaryDto`, avoiding the overhead of loading full entities.
-   **Dynamic Search with Pagination**: The `/api/users/search` endpoint uses the `Specification` pattern to provide dynamic, type-safe filtering combined with pagination and sorting.

### 2. Advanced Transaction Management

The project demonstrates three distinct approaches to transaction management, which can be tested via the `/api/transactions/**` endpoints.

-   **Declarative (`@Transactional`)**: The `TransactionalUserService` showcases advanced attributes like `propagation` (e.g., `REQUIRES_NEW`), `isolation`, and `rollbackFor` a custom checked exception.
-   **Programmatic (`TransactionTemplate`)**: The `ProgrammaticTransactionService` provides an example of fine-grained, manual transaction control.
-   **AOP-Based (Centralized)**: The `AopConfig` defines a single pointcut and advice to apply transactions to all service-layer methods, ensuring consistency without scattering annotations.

### 3. Concurrency & Event-Driven Architecture

-   **Custom Thread Pool**: The `AsyncConfig` class defines a custom `ThreadPoolTaskExecutor`, which is a production best practice for managing resources for asynchronous tasks.
-   **Decoupled Event Handling**: User registration is now event-driven. The `AuthController` publishes a `UserRegisteredEvent`, which is handled by an asynchronous `@EventListener` in `UserRegistrationListener`. This decouples the registration logic from subsequent actions like sending emails.

---

## Getting Started

### Prerequisites

-   JDK 17 or later
-   Gradle
-   Docker (for running Zipkin and the application container)

### Running the Application

1.  **Run the Application**:
  ```bash
  ./gradlew clean bootRun
  ```
2.  **Run Supporting Services (Zipkin)**:
  ```bash
  docker run -d -p 9411:9411 openzipkin/zipkin
  ```
  Access the Zipkin UI at `http://localhost:9411`.

### Building and Running with Docker

```bash
# Build the Docker image
docker build -t spring-jpa-poc .

# Run the container
docker run -p 8080:8080 spring-jpa-poc
```

---

## API Endpoints

### Authentication

-   **Register a new user**: `POST /api/auth/register`
-   **Login to get a JWT**: `POST /api/auth/login`

### User Search & Projections

-   **Dynamic User Search**: `GET /api/users/search`
  -   *Filtering*: `?name=john&role=ADMIN`
  -   *Pagination*: `?page=0&size=10`
  -   *Sorting*: `?sort=name,asc&sort=email,desc`
-   **Get User Summaries (DTO Projection)**: `GET /api/users/summaries`

### Transaction Demos

-   **Declarative Rollback**: `GET /api/transactions/declarative-rollback`
-   **Declarative Propagation**: `GET /api/transactions/declarative-propagation`
-   **Programmatic Commit/Rollback**: `GET /api/transactions/programmatic-commit` or `.../programmatic-rollback`
-   **AOP-Based Commit/Rollback**: `GET /api/transactions/aop-commit` or `.../aop-rollback`

### Other Endpoints

-   **Resilience Demo**: `GET /api/resilience/fetch`
-   **Async Task Demo**: `GET /api/async/long-task`

### Database

-   **H2 Console**: `http://localhost:8080/h2-console`
-   **JDBC URL**: `jdbc:h2:mem:testdb` (user: `sa`, no password).

---

## Optional Enhancements & Judgment Calls (for future pursuit)

### Optional Enhancements
- Flyway baseline migration to replace ddl-auto
- Logback prod profile with rolling policies
- Kubernetes readiness/liveness probe examples in README
- Encryption / masking strategy for logs

### Judgment Calls
- Split config into dev/prod profiles
- Parameterized resilience and DB pooling via env vars
- Health liveness/readiness grouping in prod
- Limited actuator exposure in prod
- Management port separation and graceful shutdown alignment
- Data.sql preserved for demo/test seeding
- Comments guiding secret override and alerting

---
