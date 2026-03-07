package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ServiceStatusResponse(
    UUID serviceId,
    String serviceName,
    String environment,
    ServiceStatus currentStatus,
    Instant lastSnapshotAt,
    List<ServiceStatusHistoryItemResponse> history,
    List<HealthSnapshotResponse> recentSnapshots,
    boolean cached) {}
