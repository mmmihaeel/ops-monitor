# Roadmap

The current repository is intentionally disciplined in scope. The roadmap focuses on extending operational depth without erasing the design choices that already make the project readable.

Related reading: [README](../README.md), [Deployment Notes](deployment-notes.md), [Security](security.md)

## Current Baseline

Implemented today:

- snapshot-driven service status derivation
- guarded failed-job retry with retry attempts and audit entries
- incident lifecycle with explicit transitions
- Redis-backed summary caching and retry locking
- Docker-first local workflow and CI validation

The items below are extensions, not hidden features.

## Planned Direction

| Horizon | Candidate improvement | Why it is the logical next step |
| --- | --- | --- |
| Near term | Rate limiting for mutating operator endpoints | Protects the control plane from accidental or abusive write bursts |
| Near term | Incident ownership metadata and on-call assignment fields | Deepens incident accountability without changing the core lifecycle |
| Near term | Failed-job registration endpoint for upstream systems | Removes dependence on seeded demo data for failure intake |
| Near term | Audit export to external SIEM or log pipelines | Extends the current audit model into a broader operational ecosystem |
| Mid term | Token or API-key auth with persistent principals | Keeps the role model while upgrading the identity story |
| Mid term | Asynchronous retry executor behind the current retry command | Preserves the control-plane API while adding real execution infrastructure |
| Mid term | Notifications for critical status transitions and incident lifecycle changes | Turns the current state model into an alertable workflow |
| Mid term | Summary views segmented by environment or owner team | Makes the public health surface more useful at higher scale |
| Long term | Policy-driven retry profiles by job type or criticality | Moves retry behavior from code-only defaults to operational policy |
| Long term | Correlation IDs spanning snapshots, retries, incidents, and audit entries | Improves traceability across related operational records |
| Long term | Multi-project or tenant partitioning | Introduces stronger access scoping for broader operational use |
| Long term | Additional deployment targets such as Kubernetes manifests | Broadens deployment realism without changing the core backend model |

## What Is Intentionally Not Claimed Yet

- no queue worker currently consumes `nextRetryAt`
- no external identity provider is integrated
- no assignment, escalation, or paging engine exists for incidents
- no tenant isolation model is implemented

That boundary is deliberate. It keeps the project honest today and makes future evolution easy to explain.
