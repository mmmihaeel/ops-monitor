# Local Development

The default workflow is Docker-first. That keeps the repository easy to evaluate and ensures PostgreSQL, Redis, and the application all run with the same assumptions used by the tests and documentation.

Related reading: [README](../README.md), [Deployment Notes](deployment-notes.md), [Security](security.md)

## Prerequisites

- Docker Engine
- Docker Compose plugin

No local PostgreSQL or Redis installation is required for the main workflow.

## Local Stack

| Service | Purpose | Local port |
| --- | --- | --- |
| `app` | Spring Boot API | `3005` |
| `postgres` | durable data store | `5437` |
| `redis` | cache and retry lock | `6384` |
| `maven` | formatting, tests, packaging | none; invoked through Compose profile |

## Quick Start

```bash
cp .env.example .env
docker compose up -d --build app
curl http://localhost:3005/api/v1/health
curl -u ops_management:ops_management_password http://localhost:3005/actuator/health
```

For PowerShell, use `Copy-Item .env.example .env` instead of `cp`.

## Endpoints and Credentials

| Surface | Address |
| --- | --- |
| API base | `http://localhost:3005` |
| Swagger UI | `http://localhost:3005/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:3005/api-docs` |
| Actuator health | `http://localhost:3005/actuator/health` |
| PostgreSQL | `localhost:5437` |
| Redis | `localhost:6384` |

| Role | Default username | Default password |
| --- | --- | --- |
| Viewer | `ops_viewer` | `ops_viewer_password` |
| Operator | `ops_operator` | `ops_operator_password` |
| Admin | `ops_admin` | `ops_admin_password` |
| Management | `ops_management` | `ops_management_password` |

All values can be overridden in `.env`.

## Seed Data

Flyway runs automatically at startup and currently seeds demo data through `V2__seed_demo_data.sql`. That gives the repository an immediately useful local state.

| Record | Identifier | Purpose |
| --- | --- | --- |
| `payments-api` | `11111111-1111-1111-1111-111111111111` | healthy production-facing API example |
| `orders-worker` | `22222222-2222-2222-2222-222222222222` | degraded worker example with retryable failed job |
| `email-dispatcher` | `33333333-3333-3333-3333-333333333333` | down staging service with acknowledged incident |
| failed job | `44444444-1111-1111-1111-111111111111` | retryable reconciliation failure |
| incident | `66666666-2222-2222-2222-222222222222` | acknowledged critical incident example |

Those records make it easy to exercise:

- service status drill-down
- failed-job retry
- incident queries
- audit filtering

## Validation Commands

| Task | Command |
| --- | --- |
| Show logs | `docker compose logs -f app` |
| Validate Compose file | `docker compose config > /dev/null` |
| Format check | `docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw spotless:check"` |
| Run tests | `docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw test"` |
| Build package | `docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw -DskipTests package"` |
| Stop stack | `docker compose down` |
| Reset state | `docker compose down -v` |

If you run `./mvnw test` outside Docker, PostgreSQL and Redis still need to be available. The default test profile points at the same local ports used by Compose and resets Flyway plus Redis state between integration tests.

## Local Review Path

A good reviewer flow after startup is:

1. inspect the public health summary
2. read a service status view using the seeded `orders-worker` service ID
3. trigger a retry on the seeded failed job as `ops_operator`
4. query audit entries as `ops_admin`

That sequence exercises the project's strongest behavior without needing any extra setup.
