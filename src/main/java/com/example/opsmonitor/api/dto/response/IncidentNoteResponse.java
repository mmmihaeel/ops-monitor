package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import com.example.opsmonitor.domain.model.enums.IncidentStatus;
import java.time.Instant;
import java.util.UUID;

public record IncidentNoteResponse(
    UUID id,
    UUID serviceId,
    UUID failedJobId,
    IncidentSeverity severity,
    IncidentStatus status,
    String title,
    String note,
    String author,
    Instant acknowledgedAt,
    String acknowledgedBy,
    Instant resolvedAt,
    String resolvedBy,
    Instant createdAt) {}
