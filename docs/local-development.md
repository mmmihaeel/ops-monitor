# Local Development

## Prerequisites

- Docker Engine
- Docker Compose plugin

No local JDK or PostgreSQL/Redis installation is required for the default workflow.

## Quick Start

1. Create local environment file:

```bash
cp .env.example .env
```

2. Start application stack:

```bash
docker compose up -d --build app
```

3. Verify public health endpoint:

```bash
curl http://localhost:3005/api/v1/health
```

4. Verify management health endpoint:

```bash
curl -u ops_management:ops_management_password http://localhost:3005/actuator/health
```

## Useful Commands

### Tail logs

```bash
docker compose logs -f app
```

### Run format check

```bash
docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw spotless:check"
```

### Run tests

```bash
docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw test"
```

### Build package

```bash
docker compose --profile tooling run --rm maven bash -lc "chmod +x mvnw && ./mvnw -DskipTests package"
```

### Stop stack

```bash
docker compose down
```

### Reset database and redis state

```bash
docker compose down -v
```

## Local Endpoints

- API base: `http://localhost:3005`
- Swagger UI: `http://localhost:3005/swagger-ui.html`
- OpenAPI JSON: `http://localhost:3005/api-docs`
- Actuator: `http://localhost:3005/actuator/health`
- PostgreSQL: `localhost:5437`
- Redis: `localhost:6384`

## Default Credentials

API roles:
- viewer: `ops_viewer` / `ops_viewer_password`
- operator: `ops_operator` / `ops_operator_password`
- admin: `ops_admin` / `ops_admin_password`

Management:
- `ops_management` / `ops_management_password`

Override values in `.env` as needed.

## Seed Data

Flyway migrations include seeded demo records for:
- monitored services
- health snapshots and status history
- failed jobs and retry attempts
- incidents
- audit entries

Seed identifiers are documented in [../README.md](../README.md).
