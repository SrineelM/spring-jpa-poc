
docker build -t spring-jpa-poc .
docker run -p 8080:8080 spring-jpa-poc
<div align="center">

# Production-Ready Spring Boot 3 Showcase

End‑to‑end reference implementation demonstrating pragmatic, production‑grade patterns: persistence performance, transactional strategies, resilience, security (JWT), observability, rate limiting, async & events, and operational ergonomics.

</div>

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Stack & Dependencies](#stack--dependencies)
3. [Module / Package Structure](#module--package-structure)
4. [Data Persistence & Performance](#data-persistence--performance)
5. [Transactional Strategies](#transactional-strategies)
6. [Domain Events & Asynchronous Execution](#domain-events--asynchronous-execution)
7. [Security & JWT Flow](#security--jwt-flow)
8. [Resilience Patterns](#resilience-patterns)
9. [Rate Limiting](#rate-limiting)
10. [Caching Strategy](#caching-strategy)
11. [Repository Patterns & Advanced SQL](#repository-patterns--advanced-sql)
12. [DTOs & Projections](#dtos--projections)
13. [Specifications (Dynamic Filtering)](#specifications-dynamic-filtering)
14. [Observability (Logging, Metrics, Tracing, Health)](#observability-logging-metrics-tracing-health)
15. [Configuration & Profiles](#configuration--profiles)
16. [Build & Run](#build--run)
17. [API Quick Reference](#api-quick-reference)
18. [Sample cURL Usage](#sample-curl-usage)
19. [Testing Notes](#testing-notes)
20. [Troubleshooting](#troubleshooting)
21. [Future Enhancements](#future-enhancements)

---

## Architecture Overview
Layered architecture with clear vertical slices: 

| Layer | Responsibility | Key Artifacts |
|-------|----------------|---------------|
| Web (Controllers) | HTTP boundary, validation, mapping, status codes | `web/*Controller` |
| Service | Orchestrates domain operations, transactions, resilience | `service/*Service` |
| Domain | Entities + listeners + value objects | `domain/*` |
| Repository | Persistence access (Spring Data + custom criteria + native) | `repository/*` |
| Security | Authentication, token issuance & filter chain | `security/*` |
| Cross-cutting | AOP, async, rate limit, observability | `config/*`, `interceptor/*`, `health/*` |

State is kept minimal (stateless services + immutable DTO responses) enabling horizontal scaling.

## Stack & Dependencies
Core: Spring Boot 3.5.x, Spring Data JPA, Spring Security, Micrometer, Resilience4j, Caffeine, JJWT, Logstash Logback, H2 (dev) / external DB (prod). See `build.gradle` for full list with inline rationale comments.

## Module / Package Structure
```
config/            – AOP, async, JPA, logging, observability, rate limiting, security config
domain/            – Entities, converters, auditing, entity listeners
dto/               – Transport objects & projections
event/             – Domain events + async listeners
exception/         – Centralized handler + rich exception types
health/            – Custom health contributors
interceptor/       – Rate limiting interceptor
repository/        – Spring Data + custom impl + window/native queries
security/          – JWT, user details, auth filter & entry point
service/           – Business logic, transactions, resilience patterns
spec/              – Specification factory for dynamic filtering
web/               – REST controllers (auth, users, monitoring, resilience, transactions, async)
resources/         – Profiles, logging, seed data
```

## Data Persistence & Performance
Highlights:
* N+1 avoidance: `@EntityGraph("User.withProfileAndPosts")` & `JOIN FETCH` query variants.
* Window function ranking: `UserWindowRepository#getUserRankingRaw()` uses native SQL (RANK + COUNT OVER) → mapped to `UserRankDTO` at service layer.
* DTO projection: JPQL constructor expression for `UserSummaryDto` reduces hydration overhead.
* Custom criteria: `CustomUserRepositoryImpl#findUsingCriteria` builds dynamic predicates.
* Dynamic search: `UserService#searchUsers` composes specifications safely (uses a neutral `cb.conjunction()` instead of deprecated `Specification.where(null)`).

## Transactional Strategies
| Style | Location | Use Case |
|-------|----------|----------|
| Declarative | `TransactionalUserService` | Standard CRUD with propagation / isolation nuances |
| Programmatic | `ProgrammaticTransactionService` | Fine‑grained manual boundary + conditional rollback |
| Centralized AOP | `AopConfig` + `AopTransactionalService` | Cross-cut transaction rule without per-method annotations |

Demo endpoints under `TransactionDemoController` illustrate rollback semantics (checked vs unchecked), propagation (`REQUIRES_NEW`), and programmatic control.

## Domain Events & Asynchronous Execution
* Event publishing on registration: `AuthController` → `UserRegisteredEvent`.
* Async listener: `UserRegistrationListener` (`@Async @EventListener`) decouples side-effects (email simulation, provisioning) from request latency.
* Synchronous variant: `UserCreatedEvent` + `UserCreatedListener` for immediate pipeline reactions.
* Async tasks: `AsyncService#performLongRunningTask` uses configured executor in `AsyncConfig`.

## Security & JWT Flow
1. Login (`/api/auth/login`) authenticates via `AuthenticationManager`.
2. On success, `JwtTokenProvider#generateToken` issues signed HS512 JWT (subject = email, minimal claims).
3. Each request passes through `JwtAuthenticationFilter`:
   * Extracts Bearer token → validates signature + expiry.
   * Loads user via `CustomUserDetailsService` (authorities from Role enum).
   * Populates `SecurityContext` → downstream `@PreAuthorize` decisions.
4. Failures return 401 via `JwtAuthenticationEntryPoint`.
5. Stateless design (no server session). Token secret driven by profile / environment.

Hardening Notes:
* Rotate secrets & optionally embed `kid` header for key rotation.
* Add refresh token flow if session longevity required.
* Expand authorities if moving beyond single-role design.

## Resilience Patterns
Implemented in `ExternalApiService#fetchData` with stacked annotations:
* CircuitBreaker – opens on failure threshold (configurable via properties) to fail fast.
* Retry – controlled backoff for transient faults.
* TimeLimiter – bounds latency for async result.
* Bulkhead – concurrency isolation (thread/semaphore) to prevent resource starvation.
* RateLimiter – protects external system & local resources from burst overuse.
Fallback path aggregates context (correlation ID via MDC) for diagnostic clarity.

## Rate Limiting
`RateLimitInterceptor` implements a lightweight in‑memory token bucket:
* Separate thresholds for auth vs general endpoints.
* Buckets keyed by client IP / X-Forwarded-For.
* Opportunistic cleanup of stale buckets; production suggestion: replace map with Caffeine + expiry.
* Returns JSON 429 with simple error contract.

## Caching Strategy
* Caffeine configured (`application.yml`) with size + access expiry for hot user data.
* Method-level caching examples: `UserService#findById`, `UserService#searchUsers`, aggregated stats.
* Write paths log need for cache eviction (illustrative; add `@CacheEvict` in real deployment).

## Repository Patterns & Advanced SQL
* Standard CRUD via `JpaRepository`.
* Specifications & QBE (foundation present; QBE extension trimmed for simplicity).
* Stored procedure demo (`@Procedure(name = "User.countByRole")`).
* Native window functions for ranking (see above).
* Bulk update (`updateName`) shows cost-saving write without entity hydration (be mindful of stale persistence context cache).

## DTOs & Projections
Purpose-built DTO classes under `dto/` with extensive inline comments:
* Authentication payload (`JWTAuthResponse`, `LoginDto`, `SignUpDto`).
* Summaries / rankings (`UserSummaryDto`, `UserRankDTO`).
* Canonical `UserDTO` for minimalist identity transfer.

## Specifications (Dynamic Filtering)
`UserSpecifications` builds small, composable spec fragments. Chaining avoids large conditional monolith queries. Neutral starting point uses `cb.conjunction()` to prevent deprecated patterns.

## Observability (Logging, Metrics, Tracing, Health)
* Structured JSON logging (`logback-spring.xml` + Logstash encoder) → ready for ELK / OpenSearch ingestion.
* Global error contract: `GlobalExceptionHandler` returns rich `ErrorDetails` (timestamp, code, validation map).
* Metrics: Micrometer timers & counters annotate auth, external calls, health, resilience endpoints.
* Tracing: Micrometer Tracing + Zipkin exporter (`@NewSpan`, `@SpanTag` in `UserService`).
* Custom health indicator: `DatabaseHealthIndicator` provides latency & SQL error diagnostics.
* Monitoring endpoints: `MonitoringController` surfaces composite system + JVM + DB + application insights.

## Configuration & Profiles
Files:
* `application.yml` – shared baseline (resilience defaults, caching, tracing sample rate).
* `application-dev.yml` – H2, full SQL logging, 100% trace sampling, developer conveniences.
* `application-prod.yml` – external DB, reduced actuator surface, limited trace sampling, security headers & management port separation.
* `data.sql` – deterministic seed data (suitable for demo / tests; replace with migrations in prod).
Environment variable overrides (examples):
```
CB_WINDOW_SIZE, CB_FAILURE_THRESHOLD, JWT_EXPIRY_MS, JWT_SECRET, DB_URL, DB_USER, DB_PASSWORD
```

## Build & Run
### Local (Windows PowerShell)
```powershell
./gradlew.bat clean bootRun
```

### Zipkin (optional tracing UI)
```powershell
docker run -d -p 9411:9411 openzipkin/zipkin
```
Visit http://localhost:9411

### Docker Image
```powershell
docker build -t spring-jpa-poc .
docker run -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev spring-jpa-poc
```

### JVM / Profile Overrides
```powershell
./gradlew.bat bootRun -Dspring.profiles.active=prod -Dapp.jwt-expiration-milliseconds=7200000
```

## API Quick Reference
| Category | Endpoint | Notes |
|----------|----------|-------|
| Auth | POST /api/auth/login | Returns JWT (Bearer) |
| Auth | POST /api/auth/register | Publishes registration event |
| Users | GET /api/users/search | name/email/role filters + paging |
| Users | GET /api/users/summaries | Projection DTO list |
| Users | GET /api/users/{id} | Cached lookup |
| Users | GET /api/users/stats | Admin only |
| Transactions | /api/transactions/* | Demonstration endpoints |
| Resilience | GET /api/resilience/fetch | Exercises resilience stack |
| Async | GET /api/async/long-task | Returns `CompletableFuture` |
| Monitoring | GET /api/monitoring/health | Composite health JSON |
| Monitoring | GET /api/monitoring/metrics | System + app metrics |

## Sample cURL Usage
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"name":"Alice","email":"alice@example.com","password":"Abcdef1!"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"alice@example.com","password":"Abcdef1!"}' | jq -r .accessToken)

# Authenticated search
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/users/search?name=ali&page=0&size=5"
```

## Testing Notes
* JUnit 5 platform enabled; security test utilities included.
* Recommended additions (not yet implemented):
  * Slice tests (`@DataJpaTest`, `@WebMvcTest`).
  * Contract tests for error handler response schema.
  * Resilience pattern tests using `TestScheduler` / controlled failure injection.

## Troubleshooting
| Issue | Likely Cause | Resolution |
|-------|--------------|------------|
| 401 Unauthorized | Missing / malformed Bearer token | Login → supply `Authorization: Bearer <token>` |
| 429 Too Many Requests | Rate limiter exceeded | Wait for refill interval or adjust config |
| Circuit Breaker Open | Downstream simulated failures hit threshold | Inspect `/actuator/health` & logs |
| H2 console not available | Wrong profile | Ensure `dev` profile active |
| Stale cached user data | Cache not evicted on mutation | Add `@CacheEvict` annotations |

## Future Enhancements
Category | Idea
---------|-----
Migrations | Introduce Flyway/Liquibase baseline; drop `ddl-auto`
Security | Refresh tokens, password policy service, role hierarchy
Observability | OpenTelemetry exporter / exemplars, log redaction filters
Resilience | Per-endpoint custom configs & adaptive rate limiting
Caching | Second-level Hibernate cache (selective) & cache invalidation events
Docs | OpenAPI enrichment (response schemas & examples)
CI/CD | Add GitHub Actions workflow: build → test → docker push
Infra | K8s manifests with liveness/readiness + HPA hints

## Optional Enhancements (Original Backlog)
These were part of the initial optional backlog and are preserved verbatim for traceability:
* Flyway baseline migration to replace `ddl-auto`
* Logback prod profile with rolling policies
* Kubernetes readiness/liveness probe examples in README
* Encryption / masking strategy for sensitive log fields

## Judgment Calls (Deliberate Decisions Made)
Documenting conscious trade‑offs taken in this reference implementation:
* Split config into separate `dev` / `prod` profiles (clarity over DRY reduction)
* Parameterized resilience + DB pooling via environment variables for ops flexibility
* Health liveness/readiness grouping deferred (actuator exposure minimized instead)
* Limited actuator exposure in prod for attack surface reduction
* Management port separation + graceful shutdown alignment (foundation in config)
* `data.sql` retained for deterministic demo seed (acceptable in non‑prod only)
* Extensive inline comments to guide secret override strategy & future alerting hooks
* Avoided premature addition of refresh tokens to keep auth surface focused
* Chose in‑memory rate limiter (simple) over distributed implementation for tutorial scope

---

Feel free to fork, explore, and adapt. Contributions welcome via PRs (tests + concise rationale in description). Enjoy building resilient, observable services!

