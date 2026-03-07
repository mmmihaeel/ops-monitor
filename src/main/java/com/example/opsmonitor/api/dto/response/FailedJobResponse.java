package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.FailedJobState;
import java.time.Instant;
import java.util.UUID;

public record FailedJobResponse(
    UUID id,
    UUID serviceId,
    String serviceName,
    String externalJobId,
    String jobType,
    FailedJobState state,
    String failureReason,
    String payload,
    int retryCount,
    int maxRetries,
    Instant lastFailureAt,
    Instant nextRetryAt,
    Instant createdAt,
    Instant updatedAt) {}
