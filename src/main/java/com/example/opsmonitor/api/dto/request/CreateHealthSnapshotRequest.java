package com.example.opsmonitor.api.dto.request;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.domain.model.enums.SnapshotSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateHealthSnapshotRequest(
    @NotNull UUID serviceId,
    @NotNull ServiceStatus status,
    @Min(0) @Max(120000) Integer latencyMs,
    @Size(max = 500) String errorMessage,
    @NotNull SnapshotSource source,
    Instant recordedAt) {}
