# API Overview

Base path: `/api/v1`

The API is organized around operational workflows instead of generic entity administration. Read paths are optimized for status visibility, while write paths capture lifecycle transitions and audit signals.

Related reading: [README](../README.md), [Architecture](architecture.md), [Security](security.md)

## Access Model

| Access level | Coverage |
| --- | --- |
| Public | `GET /health`, `GET /health/readiness`, Swagger/OpenAPI endpoints |
| `VIEWER` | Read-only operational resources under `/api/v1/**` |
| `OPERATOR` | Viewer access plus snapshot ingestion, failed-job retry, and incident lifecycle actions |
| `ADMIN` | Operator access plus service registration and audit-entry access |
| `MANAGEMENT` | Separate credential for `/actuator/**` |

API roles do not authorize management endpoints.

## Response Contract

| Response type | Shape |
| --- | --- |
| Success | `data` plus `meta.timestamp`; paginated endpoints also include `meta.page` |
| Error | `code`, `message`, `timestamp`, and optional `errors[]` for field-level validation failures |

This gives list endpoints a consistent wrapper while keeping single-resource responses simple.

## Endpoint Families

| Family | Routes | Behavior | Access |
| --- | --- | --- | --- |
| Health | `GET /health`, `GET /health/readiness` | Global posture and readiness checks | Public |
| Services | `GET /services`, `GET /services/{id}`, `GET /services/{id}/status`, `POST /services` | Service registry, service detail, service status drill-down, service creation | Read `VIEWER+`, create `ADMIN` |
| Health snapshots | `GET /health-snapshots`, `POST /health-snapshots` | Review or ingest health signals | Read `VIEWER+`, write `OPERATOR+` |
| Failed jobs | `GET /failed-jobs`, `GET /failed-jobs/{id}`, `POST /failed-jobs/{id}/retry` | Review failure backlog and trigger retries | Read `VIEWER+`, retry `OPERATOR+` |
| Incidents | `GET /incidents`, `GET /incidents/{id}`, `POST /incidents`, `POST /incidents/{id}/acknowledge`, `POST /incidents/{id}/resolve` | Incident listing, detail, creation, acknowledgement, resolution | Read `VIEWER+`, write `OPERATOR+` |
| Audit entries | `GET /audit-entries` | Query sensitive action history | `ADMIN` |

## Query, Pagination, and Sort Rules

| Resource | Filters | Sort fields | Page size limit |
| --- | --- | --- | --- |
| Services | `q`, `environment`, `status` | `name`, `environment`, `currentStatus`, `createdAt`, `updatedAt` | 100 |
| Health snapshots | `serviceId`, `status`, `from`, `to` | `recordedAt`, `status`, `source` | 100 |
| Failed jobs | `serviceId`, `state`, `jobType` | `lastFailureAt`, `retryCount`, `state`, `createdAt` | 100 |
| Incidents | `serviceId`, `failedJobId`, `severity`, `status` | `createdAt`, `severity`, `title` | 100 |
| Audit entries | `entityType`, `action`, `from`, `to` | `createdAt`, `action`, `entityType` | 200 |

List responses include:

- `meta.page.page`
- `meta.page.size`
- `meta.page.totalElements`
- `meta.page.totalPages`
- `meta.page.hasNext`
- `meta.page.hasPrevious`

Sort fields are allow-listed in controller code. Unsupported sort columns are rejected before repository execution.

## Operational Behavior Notes

### Health Summary

`GET /health` returns application-wide posture derived from:

- current service counts by status
- active failed jobs in `FAILED`, `RETRY_IN_PROGRESS`, `RETRY_SCHEDULED`, or `EXHAUSTED`
- active incidents in `OPEN` or `ACKNOWLEDGED`
- incident count created in the last 24 hours

The response includes a `cached` flag because the summary is backed by Redis.

### Service Status Drill-Down

`GET /services/{id}/status` returns:

- current service status
- last snapshot timestamp
- up to 10 most recent status-history items
- up to 5 most recent snapshots
- a `cached` flag

This endpoint is the main operational drill-down surface for a single service.

### Failed-Job Retry

`POST /failed-jobs/{id}/retry` is a control-plane action, not a fire-and-forget worker trigger. The request carries:

- `requestedBy`
- `outcome`
- optional `message`

The endpoint validates retryability, acquires a Redis lock, writes a retry-attempt record, updates the failed-job state, emits an audit entry, and returns both the updated failed job and the new retry attempt.

### Incident Lifecycle

- `POST /incidents` creates incidents in `OPEN`
- `POST /incidents/{id}/acknowledge` is valid only from `OPEN`
- `POST /incidents/{id}/resolve` is valid only from `ACKNOWLEDGED`

Invalid lifecycle transitions return `409 CONFLICT`.

## Interactive API Documentation

| Surface | Path |
| --- | --- |
| Swagger UI | `/swagger-ui.html` |
| OpenAPI JSON | `/api-docs` |

For endpoint-by-endpoint behavior, the generated OpenAPI view is the best companion to this repository-level overview.
