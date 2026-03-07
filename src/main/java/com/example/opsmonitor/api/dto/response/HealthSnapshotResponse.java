package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.domain.model.enums.SnapshotSource;
import java.time.Instant;
import java.util.UUID;

public record HealthSnapshotResponse(
    UUID id,
    UUID serviceId,
    ServiceStatus status,
    Integer latencyMs,
    String errorMessage,
    SnapshotSource source,
    Instant recordedAt) {}
