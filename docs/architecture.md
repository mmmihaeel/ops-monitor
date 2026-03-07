# Architecture

## Purpose

`ops-monitor` provides a focused operational backend for service health tracking, failed-job retry workflows, incident lifecycle management, and action auditability.

The architecture prioritizes explicit business rules and readable boundaries over framework-heavy abstractions.

## Layered Structure

### API Layer (`com.example.opsmonitor.api`)

Responsibilities:
- expose HTTP endpoints under `/api/v1`
- enforce request shape and query bounds
- return consistent response envelopes
- translate exceptions into stable API error contracts

### Application Layer (`com.example.opsmonitor.application`)

Responsibilities:
- orchestrate domain workflows and transitions
- derive service status from snapshot signals
- enforce retry eligibility and lifecycle progression
- enforce incident state transitions
- emit audit entries for sensitive operations
- manage cache invalidation after writes

Key services:
- `MonitoredServiceService`
- `HealthSnapshotService`
- `ServiceStatusQueryService`
- `HealthSummaryService`
- `FailedJobService`
- `IncidentNoteService`
- `AuditEntryService`
- `ServiceStatusPolicy`

### Domain Layer (`com.example.opsmonitor.domain`)

Responsibilities:
- persistence entities and enum state models
- explicit status/lifecycle representations (`ServiceStatus`, `FailedJobState`, `IncidentStatus`)

### Infrastructure Layer (`com.example.opsmonitor.infrastructure`)

Responsibilities:
- JPA repositories and query specifications
- Redis adapters for cache and retry lock
- security and credential wiring
- runtime configuration properties
- OpenAPI metadata

## Runtime Topology

Docker Compose services:
- `app` (Spring Boot API)
- `postgres` (system of record)
- `redis` (cache + retry lock)
- `maven` (tooling profile for build/test commands)

## Workflow Design

### Health Snapshot Ingestion

1. Request is validated by controller DTO constraints.
2. `HealthSnapshotService` derives effective status using `ServiceStatusPolicy`.
3. Snapshot is persisted.
4. `MonitoredService.currentStatus` and `lastSnapshotAt` are updated.
5. `ServiceStatusHistory` row is created on status change.
6. Audit entries capture snapshot and status transition events.
7. Global and per-service status cache keys are evicted.

### Failed-Job Retry

1. Operator issues retry command with explicit outcome.
2. Redis lock (`RetryLockService`) prevents concurrent retries for the same job.
3. Retryability is validated against job state and max-retry budget.
4. Job state/retry counters are updated.
5. `RetryAttempt` record is persisted.
6. Audit entry captures actor, attempt, outcome, and resulting state.
7. Status caches are invalidated and lock is released.

### Incident Lifecycle

1. Incident is created in `OPEN` state.
2. `OPEN -> ACKNOWLEDGED` transition requires operator action.
3. `ACKNOWLEDGED -> RESOLVED` transition requires operator action.
4. Invalid transitions produce `409 CONFLICT`.
5. Lifecycle transitions are audited and trigger cache invalidation.

## Security Boundaries

- Public routes: health summary/readiness and API docs
- API roles: viewer, operator, admin
- Management routes (`/actuator/**`): dedicated management credential

Detailed auth model: [security.md](security.md)

## Data and Consistency

- PostgreSQL is source of truth for entities, history, and audit records.
- Flyway manages schema and seeded demo data.
- Service-level transactions keep write workflows consistent.
- Redis is an optimization/control layer, never a system of record.

## Observability and Operator Support

- Public summary endpoint for quick operational posture
- Per-service status endpoint with recent snapshots and transition history
- Audit query endpoint for forensic review of sensitive actions
- Actuator endpoints for runtime health and info (management auth)
