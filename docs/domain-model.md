# Domain Model

## Core Entities

### MonitoredService

Represents a system/service under monitoring.

Fields:
- `id` (UUID)
- `name`
- `environment`
- `ownerTeam`
- `endpointUrl`
- `currentStatus` (`UP`, `DEGRADED`, `DOWN`, `UNKNOWN`)
- `lastSnapshotAt`
- `createdAt`, `updatedAt`

### HealthSnapshot

Point-in-time health signal for a service.

Fields:
- `id` (UUID)
- `serviceId`
- `status` (effective status after policy derivation)
- `latencyMs`
- `errorMessage`
- `source` (`API`, `MANUAL`, `PROBE`)
- `recordedAt`

Policy notes:
- effective status starts from reported status
- error message can escalate to at least `DEGRADED`
- latency thresholds can escalate to `DEGRADED` or `DOWN`

### ServiceStatusHistory

Status transition log for monitored services.

Fields:
- `id` (bigint)
- `serviceId`
- `previousStatus`
- `newStatus`
- `reason`
- `changedAt`

Rows are written only when status changes.

### FailedJob

Operational record for background job failures.

Fields:
- `id` (UUID)
- `serviceId`
- `externalJobId`
- `jobType`
- `state` (`FAILED`, `RETRY_IN_PROGRESS`, `RETRY_SCHEDULED`, `RECOVERED`, `EXHAUSTED`)
- `failureReason`
- `payload`
- `retryCount`
- `maxRetries`
- `lastFailureAt`
- `nextRetryAt`
- `createdAt`, `updatedAt`

Retryability is state- and max-retry-aware.

### RetryAttempt

Immutable trace for each retry command.

Fields:
- `id` (UUID)
- `failedJobId`
- `attemptNumber`
- `requestedBy`
- `outcome` (`SUCCESS`, `FAILED`, `SKIPPED`)
- `message`
- `triggeredAt`

### IncidentNote

Operational incident context linked to service and/or failed job.

Fields:
- `id` (UUID)
- `serviceId` (nullable)
- `failedJobId` (nullable)
- `severity` (`INFO`, `WARNING`, `CRITICAL`)
- `status` (`OPEN`, `ACKNOWLEDGED`, `RESOLVED`)
- `title`
- `note`
- `author`
- `acknowledgedAt`, `acknowledgedBy`
- `resolvedAt`, `resolvedBy`
- `createdAt`

Constraints:
- at least one of `serviceId` or `failedJobId` is required
- lifecycle transitions are constrained by service logic:
  - `OPEN -> ACKNOWLEDGED`
  - `ACKNOWLEDGED -> RESOLVED`

### AuditEntry

Queryable audit event for sensitive actions.

Fields:
- `id` (bigint)
- `entityType`
- `entityId`
- `action`
- `actor`
- `detailsJson`
- `createdAt`

Key actions:
- `SERVICE_CREATED`
- `HEALTH_SNAPSHOT_RECORDED`
- `SERVICE_STATUS_CHANGED`
- `FAILED_JOB_RETRY_REQUESTED`
- `INCIDENT_NOTE_CREATED`
- `INCIDENT_ACKNOWLEDGED`
- `INCIDENT_RESOLVED`

## Relationships

- One `MonitoredService` has many `HealthSnapshot`
- One `MonitoredService` has many `ServiceStatusHistory`
- One `MonitoredService` has many `FailedJob`
- One `FailedJob` has many `RetryAttempt`
- One `IncidentNote` can reference a `MonitoredService`, a `FailedJob`, or both

## State Behavior Summary

- Snapshot ingestion updates current service status and optionally appends status-history rows.
- Retry commands move failed jobs through retry/recovery states and persist attempts.
- Incident lifecycle transitions are explicit and audited.
- Audit entries provide an immutable timeline for sensitive operator activity.
