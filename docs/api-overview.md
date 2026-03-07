# API Overview

Base path: `/api/v1`

## Response Contract

Successful responses use:
- `data`: endpoint payload
- `meta.timestamp`: response timestamp
- `meta.page`: pagination metadata for list endpoints

Error responses use:
- `code`
- `message`
- `errors[]` (field-level validation details when available)
- `timestamp`

## Authentication and Authorization

Public endpoints:
- `GET /health`
- `GET /health/readiness`

Role-protected API endpoints:
- `VIEWER`: read operational resources
- `OPERATOR`: viewer + write operational actions (snapshots, retries, incident transitions)
- `ADMIN`: operator + service creation + audit access

Management endpoints (`/actuator/**`) are protected separately with the `MANAGEMENT` credential.

## Endpoints

### Health

- `GET /health` (public)
  - global operational summary
  - includes service-state counts, failed jobs, active incidents
  - Redis-cached

- `GET /health/readiness` (public)
  - lightweight readiness signal

### Services

- `GET /services` (`VIEWER+`)
  - filters: `q`, `environment`, `status`
  - pagination: `page`, `size` (`size` max 100)
  - sort: `name`, `environment`, `currentStatus`, `createdAt`, `updatedAt`

- `POST /services` (`ADMIN`)
  - registers monitored service

- `GET /services/{id}` (`VIEWER+`)
  - returns service details

- `GET /services/{id}/status` (`VIEWER+`)
  - current status + recent snapshots + status history
  - Redis-cached per service

### Health Snapshots

- `POST /health-snapshots` (`OPERATOR+`)
  - records snapshot
  - triggers status derivation and transition handling

- `GET /health-snapshots` (`VIEWER+`)
  - filters: `serviceId`, `status`, `from`, `to`
  - pagination: `page`, `size` (`size` max 100)
  - sort: `recordedAt`, `status`, `source`

### Failed Jobs

- `GET /failed-jobs` (`VIEWER+`)
  - filters: `serviceId`, `state`, `jobType`
  - pagination: `page`, `size` (`size` max 100)
  - sort: `lastFailureAt`, `retryCount`, `state`, `createdAt`

- `GET /failed-jobs/{id}` (`VIEWER+`)
  - failed job details + retry attempts

- `POST /failed-jobs/{id}/retry` (`OPERATOR+`)
  - explicit retry command
  - validates retryability and records retry attempt

### Incidents

- `GET /incidents` (`VIEWER+`)
  - filters: `serviceId`, `failedJobId`, `severity`, `status`
  - pagination: `page`, `size` (`size` max 100)
  - sort: `createdAt`, `severity`, `title`

- `GET /incidents/{id}` (`VIEWER+`)
  - incident detail

- `POST /incidents` (`OPERATOR+`)
  - creates incident in `OPEN`
  - requires at least one of `serviceId` or `failedJobId`

- `POST /incidents/{id}/acknowledge` (`OPERATOR+`)
  - valid only for `OPEN`

- `POST /incidents/{id}/resolve` (`OPERATOR+`)
  - valid only for `ACKNOWLEDGED`

### Audit Entries

- `GET /audit-entries` (`ADMIN`)
  - filters: `entityType`, `action`, `from`, `to`
  - pagination: `page`, `size` (`size` max 200)
  - sort: `createdAt`, `action`, `entityType`

## Pagination Meta

List responses include:
- `meta.page.page`
- `meta.page.size`
- `meta.page.totalElements`
- `meta.page.totalPages`
- `meta.page.hasNext`
- `meta.page.hasPrevious`

## Interactive API Docs

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/api-docs`
