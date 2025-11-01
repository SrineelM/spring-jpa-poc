# Copilot Coding Agent Instructions for spring-jpa-poc

## Project Overview
This is a production-grade Spring Boot 3.5.x showcase, demonstrating advanced patterns for persistence, transactions, resilience, security (JWT), observability, rate limiting, async/event-driven flows, and operational ergonomics.

## Architecture & Major Components
- Layered structure: Controllers (`web/`), Services (`service/`), Domain Entities (`domain/`), Repositories (`repository/`), Security (`security/`), Config (`config/`), Interceptors (`interceptor/`), Health checks (`health/`), DTOs (`dto/`), Events (`event/`), Exception handling (`exception/`).
- Stateless services and immutable DTOs for scalability.
- Cross-cutting concerns (AOP, async, rate limiting, observability) are centralized in `config/`, `interceptor/`, and `health/`.

## Key Patterns & Conventions
- **Persistence**: Avoid N+1 queries using `@EntityGraph` and `JOIN FETCH`. Use DTO projections via JPQL constructor expressions. Native SQL for window functions in ranking.
- **Transactions**: Three styles—declarative (`@Transactional`), programmatic (`TransactionTemplate`), and centralized AOP (see `AopConfig`).
- **Resilience**: `ExternalApiService` uses stacked Resilience4j annotations (CircuitBreaker, Retry, TimeLimiter, Bulkhead, RateLimiter). Configurable via YAML profiles.
- **Security**: Stateless JWT authentication. `JwtAuthenticationFilter` validates tokens; `CustomUserDetailsService` loads users; `JwtTokenProvider` issues tokens. Secrets/profile-driven.
- **Rate Limiting**: In-memory token bucket in `RateLimitInterceptor`, keyed by IP. Thresholds differ for auth/general endpoints.
- **Caching**: Caffeine cache configured in YAML. Method-level caching in `UserService`.
- **Event-Driven**: Registration publishes `UserRegisteredEvent`, handled asynchronously by `UserRegistrationListener`.
- **Specification Pattern**: Dynamic filtering via `UserSpecifications`, using `cb.conjunction()` for neutral start.
- **Observability**: Structured JSON logging (`logback-spring.xml`), global error handler (`GlobalExceptionHandler`), Micrometer metrics/tracing, custom health indicator.

## Build, Run, and Test
- **Build/Run**: Use `./gradlew.bat clean bootRun` (Windows) or `./gradlew clean bootRun` (Linux/macOS).
- **Docker**: `docker build -t spring-jpa-poc .` then `docker run -p 8080:8080 spring-jpa-poc`.
- **Tracing**: Optional Zipkin: `docker run -d -p 9411:9411 openzipkin/zipkin`.
- **Profiles**: Activate with `-Dspring.profiles.active=dev` or `prod`.
- **Testing**: JUnit 5 enabled. Add slice tests (`@DataJpaTest`, `@WebMvcTest`) for coverage.

## Integration Points & External Dependencies
- **Database**: H2 for dev, external DB for prod (see YAML configs).
- **Tracing**: Zipkin via Micrometer Tracing.
- **Resilience4j**: All patterns configured in YAML.
- **JWT**: JJWT library, secret managed via profile/env.
- **Logging**: Logstash encoder for JSON logs.

## Examples & References
- See `UserService`, `ExternalApiService`, `RateLimitInterceptor`, `JwtAuthenticationFilter`, and `UserSpecifications` for key patterns.
- API endpoints and usage examples in `README.md`.

## Testing & Exception Handling Patterns
- **Exception Hierarchy**: All domain exceptions extend `DomainException` with typed error codes and HTTP status codes:
  - `UserNotFoundException` (404), `DuplicateEmailException` (409), `InsufficientPrivilegesException` (403), `InvalidStateTransitionException` (422)
  - `GlobalExceptionHandler` routes typed exceptions to correct HTTP status codes
  - Never return generic 500 for business rule violations
- **Unit Tests**: Use Mockito for repository mocking. Test files: `DomainExceptionTest.java`, `UserServiceTest.java`
- **Cache Eviction**: All write operations (`save()`, `deleteById()`) use `@CacheEvict(value="users", allEntries=true)` to prevent stale data
- **Test Naming**: Use `testMethodName_Scenario_ExpectedResult()` pattern (e.g., `testSave_DuplicateEmail()`)

## Project-Specific Guidance
- Always use DTOs for controller responses.
- Avoid deprecated `Specification.where(null)`; use `cb.conjunction()`.
- Prefer constructor-based projections for performance.
- Use async event listeners for decoupling side effects.
- Centralize cross-cutting concerns in config/interceptor packages.
- For mutations (save/delete), always add `@CacheEvict(allEntries=true)` to prevent stale cached data

## Troubleshooting
- Common issues and resolutions are documented in `README.md` (401, 429, circuit breaker, H2 console, cache).

## Future Enhancements
- See README for backlog and judgment calls (Flyway, logback, K8s, refresh tokens, etc.).

---
For more details, reference `README.md` and the respective package for each concern. If unclear, ask for clarification or examples from the codebase.
