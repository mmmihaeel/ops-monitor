# ops-monitor

`ops-monitor` is a Spring Boot backend for operational visibility and intervention across monitored services. It tracks health snapshots, derives service status, manages failed-job retries, captures incident context, and records an audit trail for sensitive actions.

## Operational Context

Teams running background workers and internal APIs need a lightweight control-plane for day-to-day operations:
- know which services are healthy, degraded, or down
- investigate failure trends and retry outcomes
- track incident lifecycle and ownership
- provide auditable action history for operator interventions

This project focuses on that workflow depth without simulating a full platform.

## Feature Highlights

- Monitored service registry with searchable listings
- Health snapshot ingestion with derived status policy
- Service status timeline endpoint with transition history
- Failed-job registry with explicit retry command and retry-attempt log
- Incident lifecycle: `OPEN -> ACKNOWLEDGED -> RESOLVED`
- Audit log endpoint for sensitive operational events
- Redis-backed status caching and retry lock coordination
- PostgreSQL schema managed through Flyway migrations with seed data
- Docker Compose local stack for app, PostgreSQL, Redis, and tooling
- CI pipeline for format checks, tests, and package build

## Technology Stack

- Java 21
- Spring Boot 3.3
- Spring Web, Data JPA, Validation, Security, Actuator
- PostgreSQL 16
- Redis 7
- Flyway
- Maven Wrapper
- JUnit 5, Spring Boot Test, MockMvc
- Spotless (google-java-format)
- Docker and Docker Compose
- GitHub Actions

## Architecture Summary

Code is organized by clear, reviewable layers:
- `api`: controllers, DTOs, response envelope, exception mapping
- `application`: business workflows (status policy, retry orchestration, incident lifecycle)
- `domain`: persistence entities and enum state models
- `infrastructure`: repositories, cache adapters, runtime/configuration wiring

Detailed design notes: [docs/architecture.md](docs/architecture.md)

## Auth and Access Model

The project uses HTTP Basic with role-specific users from environment variables.

Public routes:
- `GET /api/v1/health`
- `GET /api/v1/health/readiness`
- Swagger/OpenAPI endpoints

Protected API roles:
- `VIEWER`: read-only access to operational resources
- `OPERATOR`: viewer access plus snapshot ingestion, retry execution, incident actions
- `ADMIN`: operator access plus service registration and audit-log access

Management endpoints (`/actuator/**`) use a separate `MANAGEMENT` credential and are not shared with API roles.

Auth details and trade-offs: [docs/security.md](docs/security.md)

## Core Workflows

### Service Status Workflow

1. Operator posts a snapshot to `/api/v1/health-snapshots`.
2. Snapshot status is derived from reported status + latency + error signal.
3. `MonitoredService.currentStatus` and `lastSnapshotAt` are updated.
4. Status transition is appended to `service_status_history` when state changes.
5. Audit events are recorded for snapshot ingestion and status change.
6. Global and per-service status caches are invalidated.

### Failed-Job Retry Workflow

1. Operator triggers `POST /api/v1/failed-jobs/{id}/retry`.
2. Redis lock prevents concurrent retry of the same failed job.
3. Retryability is validated against state and max retries.
4. Job state is advanced (`RECOVERED`, `RETRY_SCHEDULED`, or `EXHAUSTED`).
5. `retry_attempt` entry is persisted.
6. Audit event records actor, outcome, and resulting state.

### Incident Workflow

1. Operator creates an incident (`OPEN`).
2. Operator acknowledges incident (`ACKNOWLEDGED`).
3. Operator resolves incident (`RESOLVED`).
4. Invalid transitions are rejected with `409 CONFLICT`.
5. Lifecycle transitions are auditable and invalidate status caches.

## Redis Usage

Redis is used in two deliberate places:
- Status caching:
  - global summary (`/api/v1/health`)
  - per-service status view (`/api/v1/services/{id}/status`)
- Retry safety:
  - short-lived lock per failed-job ID to avoid duplicate concurrent retry execution

## API Surface

Base path: `/api/v1`

Primary endpoints:
- `GET /health`
- `GET /health/readiness`
- `GET /services`
- `POST /services`
- `GET /services/{id}`
- `GET /services/{id}/status`
- `POST /health-snapshots`
- `GET /health-snapshots`
- `GET /failed-jobs`
- `GET /failed-jobs/{id}`
- `POST /failed-jobs/{id}/retry`
- `GET /incidents`
- `GET /incidents/{id}`
- `POST /incidents`
- `POST /incidents/{id}/acknowledge`
- `POST /incidents/{id}/resolve`
- `GET /audit-entries`

Full endpoint reference: [docs/api-overview.md](docs/api-overview.md)

## Local Development (Docker)

### Prerequisites

- Docker Engine
- Docker Compose plugin

### Start Stack

```bash
cp .env.example .env
docker compose up -d --build app
```

### Local Interfaces

- API: `http://localhost:3005`
- Swagger UI: `http://localhost:3005/swagger-ui.html`
- OpenAPI JSON: `http://localhost:3005/api-docs`
- Actuator health: `http://localhost:3005/actuator/health`
- PostgreSQL: `localhost:5437`
- Redis: `localhost:6384`

### Default Credentials

API users:
- `ops_viewer` / `ops_viewer_password`
- `ops_operator` / `ops_operator_password`
- `ops_admin` / `ops_admin_password`

Management user:
- `ops_management` / `ops_management_password`

All values are configurable via `.env`.

## Local Demo Walkthrough

1. Read public health summary:

```bash
curl http://localhost:3005/api/v1/health
```

2. Query service status (viewer auth):

```bash
curl -u ops_viewer:ops_viewer_password \
  http://localhost:3005/api/v1/services/22222222-2222-2222-2222-222222222222/status
```

3. Trigger failed-job retry (operator auth):

```bash
curl -u ops_operator:ops_operator_password \
  -H "Content-Type: application/json" \
  -d '{"requestedBy":"ops.oncall","outcome":"FAILED","message":"Dependency still unavailable"}' \
  http://localhost:3005/api/v1/failed-jobs/44444444-1111-1111-1111-111111111111/retry
```

4. Review audit entries (admin auth):

```bash
curl -u ops_admin:ops_admin_password \
  "http://localhost:3005/api/v1/audit-entries?action=FAILED_JOB_RETRY_REQUESTED"
```

## Testing and Quality Checks

Run inside Docker tooling container:

```bash
docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw spotless:check"
docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw test"
docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw -DskipTests package"
```

## CI

GitHub Actions workflow runs:
- Docker Compose config validation
- `spotless:check`
- full test suite
- package build

Workflow file: `.github/workflows/ci.yml`

## Repository Structure

```text
.github/workflows/        CI pipeline
docker/java/              runtime image build
docs/                     architecture and operational documentation
src/main/java/            application source code
src/main/resources/       application config and Flyway migrations
src/test/java/            integration and unit tests
```

## Additional Documentation

- [docs/architecture.md](docs/architecture.md)
- [docs/domain-model.md](docs/domain-model.md)
- [docs/api-overview.md](docs/api-overview.md)
- [docs/security.md](docs/security.md)
- [docs/local-development.md](docs/local-development.md)
- [docs/deployment-notes.md](docs/deployment-notes.md)
- [docs/roadmap.md](docs/roadmap.md)

## Current Limitations

- Auth is intentionally simple (in-memory users + HTTP Basic) for local operations and reviewer clarity.
- Retry execution is synchronous API-driven simulation, not queue-worker orchestration.
- Incident collaboration is single-step notes plus lifecycle state, without assignment/escalation rules.

## License

See `LICENSE`.
