package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import java.time.Instant;

public record ServiceStatusHistoryItemResponse(
    ServiceStatus previousStatus, ServiceStatus newStatus, String reason, Instant changedAt) {}
