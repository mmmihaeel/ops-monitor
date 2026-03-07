# Roadmap

## Near Term

- Add rate limiting for mutating operator endpoints (`health-snapshots`, `retry`, incident transitions)
- Add optional assignment metadata for incidents (owner/on-call rotation)
- Add lightweight event export for audit entries to external SIEM/log pipelines
- Add endpoint to register failed jobs from upstream systems instead of relying only on seeded/demo records

## Mid Term

- Introduce token/API-key based auth while keeping role model (`viewer/operator/admin`)
- Add asynchronous retry executor integration (queue + worker) with current retry command as control-plane trigger
- Add notification hooks for critical status transitions and incident lifecycle changes
- Expand operational summary endpoint with environment/team segment aggregates

## Long Term

- Multi-project or tenant partitioning with scoped access controls
- Policy-driven retry profiles by job type/service criticality
- Correlation IDs across snapshots, retries, incidents, and audit events
- Additional deployment manifests for Kubernetes and managed cloud data services
