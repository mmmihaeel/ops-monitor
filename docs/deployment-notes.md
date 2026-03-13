# Deployment Notes

`ops-monitor` is local-first, but its runtime shape is still production-minded: a containerized API, PostgreSQL as the system of record, Redis as a support layer, explicit config via environment variables, and startup validation through Flyway plus JPA schema checks.

Related reading: [README](../README.md), [Architecture](architecture.md), [Security](security.md)

## Runtime Packaging

The repository ships with a multi-stage Docker build in [`docker/java/Dockerfile`](../docker/java/Dockerfile).

| Stage | Base image | Purpose |
| --- | --- | --- |
| Build | `maven:3.9.9-eclipse-temurin-21` | resolve dependencies and package the Spring Boot jar |
| Runtime | `eclipse-temurin:21-jre-jammy` | run the packaged jar as a non-root `spring` user |

The image build packages the application with `-DskipTests package`. Tests run in CI and in the local tooling workflow rather than inside the image build itself.

## External Dependencies

| Dependency | Role |
| --- | --- |
| PostgreSQL | durable persistence for operational entities, status history, retry attempts, and audit records |
| Redis | global summary cache, per-service status cache, retry lock |

## Configuration Surface

Reference defaults live in [`.env.example`](../.env.example).

| Category | Variables |
| --- | --- |
| Server | `APP_PORT` |
| Database | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` |
| Redis | `REDIS_HOST`, `REDIS_PORT` |
| Management auth | `MANAGEMENT_USERNAME`, `MANAGEMENT_PASSWORD` |
| API auth | `API_VIEWER_USERNAME`, `API_VIEWER_PASSWORD`, `API_OPERATOR_USERNAME`, `API_OPERATOR_PASSWORD`, `API_ADMIN_USERNAME`, `API_ADMIN_PASSWORD` |
| Cache tuning | `CACHE_GLOBAL_SUMMARY_TTL`, `CACHE_SERVICE_STATUS_TTL` |
| Retry tuning | `RETRY_LOCK_TTL` |
| Status policy | `STATUS_DEGRADED_LATENCY_MS`, `STATUS_DOWN_LATENCY_MS` |

## Startup Sequence

At boot, the application follows a predictable startup path:

1. load environment-driven configuration
2. connect to PostgreSQL and Redis
3. run Flyway migrations
4. validate the JPA schema against the migrated database
5. expose the API and actuator endpoints

This gives the service a fail-fast posture if the schema or infrastructure wiring is wrong.

## Health and Operational Exposure

| Surface | Purpose | Access |
| --- | --- | --- |
| `GET /api/v1/health/readiness` | readiness probe for the public API surface | public |
| `GET /api/v1/health` | global operational summary | public |
| `/actuator/health` and `/actuator/info` | runtime management endpoints | `MANAGEMENT` credential |

The separation between public health and management endpoints is deliberate. Operational posture is intentionally shareable, while actuator access remains privileged.

## CI Pipeline

The GitHub Actions workflow at [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) currently performs:

- Docker Compose configuration validation
- `spotless:check`
- the test suite
- package build

That aligns the repository's local validation path with its hosted validation path.

## Deployment Posture and Boundaries

Important current-scope notes:

- Flyway includes demo seed data today, which is excellent for local review and should be separated for a production rollout.
- Authentication is environment-backed and in-memory, not connected to a persistent identity provider.
- Retry scheduling metadata exists, but the repository does not yet run an asynchronous retry worker.

## Practical Next Steps for Hosted Use

- move credentials into a managed secret store
- split demo data from production migrations
- add ingress/TLS policy and request rate limiting
- define backup, restore, and retention policy for PostgreSQL
- externalize logs, metrics, and traces
- upgrade API authentication to persistent principals plus tokens or API keys
