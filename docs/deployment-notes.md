# Deployment Notes

## Image and Runtime Strategy

`ops-monitor` is packaged as a multi-stage Docker build:
1. Build stage compiles/tests the project with Maven and Java 21.
2. Runtime stage runs the Spring Boot jar on `eclipse-temurin:21-jre-jammy` as non-root.

Runtime dependencies:
- PostgreSQL for transactional persistence
- Redis for status caching and retry locking

## Required Environment Variables

Core runtime:
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

Auth model:
- `MANAGEMENT_USERNAME`
- `MANAGEMENT_PASSWORD`
- `API_VIEWER_USERNAME`
- `API_VIEWER_PASSWORD`
- `API_OPERATOR_USERNAME`
- `API_OPERATOR_PASSWORD`
- `API_ADMIN_USERNAME`
- `API_ADMIN_PASSWORD`

Operational tuning:
- `CACHE_GLOBAL_SUMMARY_TTL`
- `CACHE_SERVICE_STATUS_TTL`
- `RETRY_LOCK_TTL`
- `STATUS_DEGRADED_LATENCY_MS`
- `STATUS_DOWN_LATENCY_MS`
- `APP_PORT`

Reference defaults: `.env.example`

## Startup Behavior

- Flyway migrations run automatically on startup.
- Application fails fast on schema mismatch (JPA `ddl-auto=validate`).
- Seed data is applied by migration scripts and intended for local/demo environments.

## Health and Management

- Public API readiness: `GET /api/v1/health/readiness`
- Public operational summary: `GET /api/v1/health`
- Actuator endpoints (`/actuator/**`) require management credentials

## CI/CD Pipeline

Workflow: `.github/workflows/ci.yml`

Checks performed:
- Docker Compose config validation
- `spotless:check`
- full test suite
- package build (`-DskipTests package`)

## Deployment Considerations

For real hosted deployment, recommended next steps:
- external secret manager for credentials
- TLS termination and ingress policy
- centralized logs/metrics/traces
- PostgreSQL backup/restore strategy
- persistent identity store for API principals
- rate limiting at API gateway layer
