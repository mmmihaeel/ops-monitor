CREATE TABLE monitored_service (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    environment VARCHAR(40) NOT NULL,
    owner_team VARCHAR(80) NOT NULL,
    endpoint_url VARCHAR(255),
    current_status VARCHAR(20) NOT NULL,
    last_snapshot_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_monitored_service_name_environment UNIQUE (name, environment)
);

CREATE TABLE health_snapshot (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    latency_ms INTEGER,
    error_message VARCHAR(500),
    source VARCHAR(20) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_health_snapshot_service FOREIGN KEY (service_id) REFERENCES monitored_service (id)
);

CREATE TABLE service_status_history (
    id BIGSERIAL PRIMARY KEY,
    service_id UUID NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    reason VARCHAR(200) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_status_history_service FOREIGN KEY (service_id) REFERENCES monitored_service (id)
);

CREATE TABLE failed_job (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    external_job_id VARCHAR(120) NOT NULL,
    job_type VARCHAR(80) NOT NULL,
    state VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(600) NOT NULL,
    payload TEXT,
    retry_count INTEGER NOT NULL,
    max_retries INTEGER NOT NULL,
    last_failure_at TIMESTAMPTZ NOT NULL,
    next_retry_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_failed_job_service FOREIGN KEY (service_id) REFERENCES monitored_service (id)
);

CREATE TABLE retry_attempt (
    id UUID PRIMARY KEY,
    failed_job_id UUID NOT NULL,
    attempt_number INTEGER NOT NULL,
    requested_by VARCHAR(80) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    message VARCHAR(500),
    triggered_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_retry_attempt_failed_job FOREIGN KEY (failed_job_id) REFERENCES failed_job (id)
);

CREATE TABLE incident_note (
    id UUID PRIMARY KEY,
    service_id UUID,
    failed_job_id UUID,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(160) NOT NULL,
    note TEXT NOT NULL,
    author VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_incident_service FOREIGN KEY (service_id) REFERENCES monitored_service (id),
    CONSTRAINT fk_incident_failed_job FOREIGN KEY (failed_job_id) REFERENCES failed_job (id),
    CONSTRAINT ck_incident_reference CHECK (service_id IS NOT NULL OR failed_job_id IS NOT NULL)
);

CREATE TABLE audit_entry (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id VARCHAR(80) NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(80) NOT NULL,
    details_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_monitored_service_status ON monitored_service (current_status);
CREATE INDEX idx_health_snapshot_service_recorded_at ON health_snapshot (service_id, recorded_at DESC);
CREATE INDEX idx_status_history_service_changed_at ON service_status_history (service_id, changed_at DESC);
CREATE INDEX idx_failed_job_state_last_failure ON failed_job (state, last_failure_at DESC);
CREATE INDEX idx_failed_job_service_state ON failed_job (service_id, state);
CREATE INDEX idx_retry_attempt_failed_job_triggered_at ON retry_attempt (failed_job_id, triggered_at DESC);
CREATE INDEX idx_incident_note_service_created_at ON incident_note (service_id, created_at DESC);
CREATE INDEX idx_incident_note_failed_job_created_at ON incident_note (failed_job_id, created_at DESC);
CREATE INDEX idx_audit_entry_created_at ON audit_entry (created_at DESC);
CREATE INDEX idx_audit_entry_entity ON audit_entry (entity_type, entity_id);
