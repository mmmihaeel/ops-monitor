package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.AuditAction;
import java.time.Instant;

public record AuditEntryResponse(
    Long id,
    String entityType,
    String entityId,
    AuditAction action,
    String actor,
    String detailsJson,
    Instant createdAt) {}
