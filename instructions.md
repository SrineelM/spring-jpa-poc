# Instructions

## Run locally (macOS / zsh)
- Requirements: Java 17+, Docker (optional for Zipkin)
- Run app (dev profile / H2):

```sh
./gradlew clean bootRun -Dspring.profiles.active=dev
```

- Optional: run Zipkin for tracing

```sh
docker run -d -p 9411:9411 openzipkin/zipkin
```

- H2 console: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:testdb
  - User: sa / Password: (empty)

## Profiles
- `dev`: local development (H2, verbose logging, 100% tracing)
- `prod`: hardened (Postgres, reduced verbosity, limited actuator)
- `jta`: enables Atomikos multi-DS demo config (for XA samples). Activate with `-Dspring.profiles.active=jta`.

## Key packages
- `web`: controllers and DTOs
- `service`: business logic and transactions (AOP/programmatic/declarative)
- `repository`: JPA repositories and custom queries
- `repository.jdbc`: Spring JDBC examples (NamedParameterJdbcTemplate)
- `security`: JWT, filters, and security configuration
- `config`: AOP, async, JPA/caching, security, observability
- `interceptor`: rate limiting
- `event`: async event-driven handlers
- `jta`: multi-datasource JTA sample (profile `jta` only)

## Testing
- Unit tests: `./gradlew test`
- Notable tests:
  - JDBC slice test: `UserJdbcRepositoryTest`
  - StepVerifier demo: `UserServiceReactiveTest`

## Postman
- See `testdata.md` for sample requests and environment variables.

## Notes
- Secrets: In prod, source JWT secret and DB credentials from environment or secret store.
- Migrations: For production, prefer Flyway over ddl-auto.
