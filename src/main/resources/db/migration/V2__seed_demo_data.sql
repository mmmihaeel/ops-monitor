INSERT INTO monitored_service (id, name, environment, owner_team, endpoint_url, current_status, last_snapshot_at, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'payments-api', 'prod', 'payments', 'https://payments.internal/health', 'UP', NOW() - INTERVAL '2 minutes', NOW() - INTERVAL '30 days', NOW() - INTERVAL '2 minutes'),
    ('22222222-2222-2222-2222-222222222222', 'orders-worker', 'prod', 'fulfillment', 'https://orders.internal/health', 'DEGRADED', NOW() - INTERVAL '4 minutes', NOW() - INTERVAL '30 days', NOW() - INTERVAL '4 minutes'),
    ('33333333-3333-3333-3333-333333333333', 'email-dispatcher', 'staging', 'communications', 'https://email.internal/health', 'DOWN', NOW() - INTERVAL '8 minutes', NOW() - INTERVAL '30 days', NOW() - INTERVAL '8 minutes');

INSERT INTO health_snapshot (id, service_id, status, latency_ms, error_message, source, recorded_at)
VALUES
    ('aaaaaaaa-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'UP', 44, NULL, 'PROBE', NOW() - INTERVAL '10 minutes'),
    ('aaaaaaaa-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'UP', 39, NULL, 'PROBE', NOW() - INTERVAL '2 minutes'),
    ('bbbbbbbb-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'UP', 120, NULL, 'PROBE', NOW() - INTERVAL '20 minutes'),
    ('bbbbbbbb-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'DEGRADED', 1900, 'Queue lag exceeded threshold', 'API', NOW() - INTERVAL '4 minutes'),
    ('cccccccc-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'DEGRADED', 3100, 'SMTP retries elevated', 'PROBE', NOW() - INTERVAL '35 minutes'),
    ('cccccccc-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'DOWN', NULL, 'SMTP provider timeout', 'API', NOW() - INTERVAL '8 minutes');

INSERT INTO service_status_history (service_id, previous_status, new_status, reason, changed_at)
VALUES
    ('22222222-2222-2222-2222-222222222222', 'UP', 'DEGRADED', 'Health snapshot recorded via API', NOW() - INTERVAL '4 minutes'),
    ('33333333-3333-3333-3333-333333333333', 'DEGRADED', 'DOWN', 'Health snapshot recorded via API', NOW() - INTERVAL '8 minutes');

INSERT INTO failed_job (id, service_id, external_job_id, job_type, state, failure_reason, payload, retry_count, max_retries, last_failure_at, next_retry_at, created_at, updated_at)
VALUES
    ('44444444-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'job-48392', 'ORDER_RECONCILIATION', 'FAILED', 'Reconciliation query timed out after 30s', '{"tenant":"acme","batch":"2026-03-07-01"}', 0, 4, NOW() - INTERVAL '15 minutes', NOW() + INTERVAL '5 minutes', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
    ('44444444-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'job-48393', 'EMAIL_SEND', 'RETRY_SCHEDULED', 'SMTP relay unavailable', '{"campaignId":"cmp-777"}', 2, 4, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '12 minutes', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '20 minutes'),
    ('44444444-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'job-48394', 'PAYMENT_WEBHOOK', 'EXHAUSTED', 'Downstream provider responded with repeated 5xx errors', '{"attemptedEvents":12}', 4, 4, NOW() - INTERVAL '5 hours', NULL, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '3 hours');

INSERT INTO retry_attempt (id, failed_job_id, attempt_number, requested_by, outcome, message, triggered_at)
VALUES
    ('55555555-1111-1111-1111-111111111111', '44444444-2222-2222-2222-222222222222', 1, 'ops.oncall', 'FAILED', 'Dependency still unavailable', NOW() - INTERVAL '40 minutes'),
    ('55555555-2222-2222-2222-222222222222', '44444444-2222-2222-2222-222222222222', 2, 'ops.oncall', 'FAILED', 'Connection reset by peer', NOW() - INTERVAL '20 minutes');

INSERT INTO incident_note (id, service_id, failed_job_id, severity, title, note, author, created_at)
VALUES
    ('66666666-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '44444444-1111-1111-1111-111111111111', 'WARNING', 'Order retries are delayed', 'Queue lag has increased to 6 minutes. Monitoring for saturation.', 'ops.oncall', NOW() - INTERVAL '12 minutes'),
    ('66666666-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', '44444444-2222-2222-2222-222222222222', 'CRITICAL', 'Email dispatch degraded', 'Escalated with provider support; fallback channel in use.', 'ops.lead', NOW() - INTERVAL '9 minutes');

INSERT INTO audit_entry (entity_type, entity_id, action, actor, details_json, created_at)
VALUES
    ('MonitoredService', '22222222-2222-2222-2222-222222222222', 'SERVICE_STATUS_CHANGED', 'api', '{"from":"UP","to":"DEGRADED","reason":"Health snapshot recorded via API"}', NOW() - INTERVAL '4 minutes'),
    ('HealthSnapshot', 'bbbbbbbb-2222-2222-2222-222222222222', 'HEALTH_SNAPSHOT_RECORDED', 'api', '{"serviceId":"22222222-2222-2222-2222-222222222222","status":"DEGRADED"}', NOW() - INTERVAL '4 minutes'),
    ('IncidentNote', '66666666-2222-2222-2222-222222222222', 'INCIDENT_NOTE_CREATED', 'ops.lead', '{"severity":"CRITICAL","title":"Email dispatch degraded"}', NOW() - INTERVAL '9 minutes');
