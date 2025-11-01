# Test Data and Sample Payloads

This project ships with `src/main/resources/data.sql` to auto-seed H2 for the `dev` profile.

## Seeded Users
- admin@example.com (role: ADMIN, password: admin)
- user1@example.com (role: USER, password: user1)
- user2@example.com (role: USER, password: user2)

## JWT Auth (example flow)
1. Authenticate (example; adjust to actual controller mapping if present):
   - POST /api/auth/login
   - Body (JSON):
     {
       "username": "admin@example.com",
       "password": "admin"
     }
   - Response includes access token (Bearer). Use it in `Authorization` header.

2. Use token:
   - Authorization: Bearer <jwt>

## Sample SQL snippets (H2)
- Find all users:
  SELECT id, email, name, role FROM users;
- Search users by name (case-insensitive):
  SELECT * FROM users WHERE LOWER(name) LIKE LOWER('%john%');

## JDBC examples
- The `UserJdbcRepository` includes methods:
  - findByEmail("user1@example.com")
  - findAll(offset, limit)
  - insert(User) -> returns rows affected

## Postman collection (DIY)
- Create a new collection "spring-jpa-poc".
- Add environment variable: baseUrl = http://localhost:8080
- Add requests:
  - GET {{baseUrl}}/actuator/health (no auth)
  - POST {{baseUrl}}/api/auth/login (Body: JSON)
  - GET {{baseUrl}}/api/users (Auth: Bearer)

## Notes
- H2 console: http://localhost:8080/h2-console (dev only). JDBC URL: jdbc:h2:mem:testdb; user: sa; password: (empty).
- For SQL dialect differences (Postgres vs H2), prefer JPA or parameterized JDBC to avoid vendor-specific syntax.
