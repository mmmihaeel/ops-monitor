# Security

`ops-monitor` uses a lightweight but intentional security model. The goal is not to mimic a full enterprise IAM stack; it is to make access boundaries, privileged operations, and auditability explicit and reviewable.

Related reading: [README](../README.md), [API Overview](api-overview.md), [Deployment Notes](deployment-notes.md)

## Security Posture

- stateless HTTP Basic authentication
- environment-backed credentials loaded at startup
- strict separation between API access and actuator management access
- validation and bounded query behavior at the request edge
- audit entries for sensitive operator actions

This is a good fit for a portfolio-scale operational backend because reviewers can see the rules directly in code without a large external auth dependency.

## Authentication Model

The application builds an `InMemoryUserDetailsManager` from environment variables and encodes passwords with BCrypt.

| Principal type | Environment variables | Granted roles |
| --- | --- | --- |
| Viewer API user | `API_VIEWER_USERNAME`, `API_VIEWER_PASSWORD` | `VIEWER` |
| Operator API user | `API_OPERATOR_USERNAME`, `API_OPERATOR_PASSWORD` | `VIEWER`, `OPERATOR` |
| Admin API user | `API_ADMIN_USERNAME`, `API_ADMIN_PASSWORD` | `VIEWER`, `OPERATOR`, `ADMIN` |
| Management user | `MANAGEMENT_USERNAME`, `MANAGEMENT_PASSWORD` | `MANAGEMENT` |

Security is configured as stateless. Sessions are not used, and CSRF is disabled because the application is not modeling browser session workflows.

## Authorization Boundaries

| Surface | Access rule |
| --- | --- |
| `GET /api/v1/health` | public |
| `GET /api/v1/health/readiness` | public |
| Swagger/OpenAPI routes | public |
| Read operations under `/api/v1/**` | `VIEWER`, `OPERATOR`, or `ADMIN` |
| `POST /api/v1/health-snapshots` | `OPERATOR` |
| `POST /api/v1/failed-jobs/{id}/retry` | `OPERATOR` |
| `POST /api/v1/incidents` and lifecycle actions | `OPERATOR` |
| `POST /api/v1/services` | `ADMIN` |
| `GET /api/v1/audit-entries` | `ADMIN` |
| `/actuator/**` | `MANAGEMENT` only |

An important design choice is that API administrators do not implicitly gain actuator access. The management plane remains separate even in local development.

## Request and Query Hardening

| Control | Implementation |
| --- | --- |
| DTO validation | Bean Validation on create and mutation requests |
| Query bounds | `page` and `size` parameters are range-limited |
| Sort safety | Controller-level allow-lists restrict sortable fields |
| Domain invariants | Services enforce retryability, incident references, and lifecycle transitions |
| Error contract | Central exception handling keeps failures explicit and stable |

This means many invalid requests are rejected before they reach repository code, and domain-specific conflicts surface as `409 CONFLICT` instead of silent no-ops.

## Auditability

The following actions produce audit entries:

- service creation
- health snapshot recording
- service status changes
- failed-job retry requests
- incident creation
- incident acknowledgement
- incident resolution

Each record stores the entity type, entity ID, action, actor, JSON details payload, and creation time. That makes the audit endpoint useful for both operational review and codebase walkthroughs.

## Retry Safety Controls

Failed-job retry uses Redis as a coordination layer:

- a lock key is acquired per failed-job ID using `setIfAbsent`
- lock lifetime is bounded by a TTL from configuration
- the lock is released in a `finally` block after retry processing
- requests are rejected if the job is already locked or no longer retryable

This is intentionally simple and pragmatic. It avoids duplicate operator actions without introducing distributed workflow infrastructure.

## Deliberate Trade-Offs

- HTTP Basic is easy to inspect and run locally, but it is not a substitute for production identity infrastructure.
- Users are not persisted; they come from environment variables at startup.
- There is no token issuance, rotation, or revocation flow in the current scope.
- Rate limiting is not implemented yet.

For a hosted deployment, the next practical upgrade would be persistent principals plus token or API-key authentication while preserving the existing viewer/operator/admin separation.
