package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import java.time.Instant;
import java.util.UUID;

public record MonitoredServiceResponse(
    UUID id,
    String name,
    String environment,
    String ownerTeam,
    String endpointUrl,
    ServiceStatus currentStatus,
    Instant lastSnapshotAt,
    Instant createdAt,
    Instant updatedAt) {}
