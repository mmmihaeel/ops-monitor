# Security

## Security Approach

`ops-monitor` uses a lightweight but deliberate model appropriate for a portfolio-scale operations backend:
- role-based API access with HTTP Basic
- strict separation between API and management credentials
- validation and bounded query parameters
- auditable sensitive actions

## Authentication Model

Credentials are configured via environment variables and loaded into an in-memory user store at startup.

API users:
- `API_VIEWER_USERNAME` / `API_VIEWER_PASSWORD` (`VIEWER`)
- `API_OPERATOR_USERNAME` / `API_OPERATOR_PASSWORD` (`VIEWER`, `OPERATOR`)
- `API_ADMIN_USERNAME` / `API_ADMIN_PASSWORD` (`VIEWER`, `OPERATOR`, `ADMIN`)

Management user:
- `MANAGEMENT_USERNAME` / `MANAGEMENT_PASSWORD` (`MANAGEMENT`)

## Access Boundaries

Public API endpoints:
- `GET /api/v1/health`
- `GET /api/v1/health/readiness`
- OpenAPI/Swagger endpoints

Protected API rules:
- `ADMIN`
  - `POST /api/v1/services`
  - `GET /api/v1/audit-entries/**`
- `OPERATOR`
  - `POST /api/v1/health-snapshots`
  - `POST /api/v1/failed-jobs/{id}/retry`
  - `POST /api/v1/incidents`
  - `POST /api/v1/incidents/{id}/acknowledge`
  - `POST /api/v1/incidents/{id}/resolve`
- `VIEWER`+
  - remaining read operations under `/api/v1/**`

Management endpoints:
- `/actuator/**` requires `MANAGEMENT` role
- API roles cannot access actuator endpoints

## Input and Query Hardening

- Bean Validation on all request DTOs
- list `size` query params capped
- sortable fields constrained by allow-lists
- domain invariants enforced in application services (retryability, incident references, lifecycle transitions)

## Audit and Forensics

Audited actions include:
- service registration
- health snapshot ingestion
- service status transition
- failed-job retry requests
- incident create/acknowledge/resolve

Audit records store actor identity and structured details JSON for forensic review.

## Retry Safety Controls

- Redis lock key per failed-job ID prevents concurrent retry execution
- lock TTL bounds lock lifetime if a request fails unexpectedly

## Trade-offs

Deliberate constraints for maintainability:
- HTTP Basic + in-memory users keeps local setup simple and explicit
- no external identity provider integration in current scope
- no token revocation flow or per-user persistence

For hosted environments, next practical upgrade is token/API-key auth backed by persistent identity storage.
