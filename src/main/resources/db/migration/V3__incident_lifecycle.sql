ALTER TABLE incident_note
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN acknowledged_at TIMESTAMPTZ,
    ADD COLUMN acknowledged_by VARCHAR(80),
    ADD COLUMN resolved_at TIMESTAMPTZ,
    ADD COLUMN resolved_by VARCHAR(80);

UPDATE incident_note
SET status = 'ACKNOWLEDGED',
    acknowledged_at = COALESCE(created_at + INTERVAL '2 minutes', NOW()),
    acknowledged_by = COALESCE(acknowledged_by, 'ops.lead')
WHERE id = '66666666-2222-2222-2222-222222222222';

CREATE INDEX idx_incident_note_status_created_at ON incident_note (status, created_at DESC);
