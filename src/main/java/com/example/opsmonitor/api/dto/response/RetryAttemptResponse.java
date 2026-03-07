package com.example.opsmonitor.api.dto.response;

import com.example.opsmonitor.domain.model.enums.RetryOutcome;
import java.time.Instant;
import java.util.UUID;

public record RetryAttemptResponse(
    UUID id,
    UUID failedJobId,
    int attemptNumber,
    String requestedBy,
    RetryOutcome outcome,
    String message,
    Instant triggeredAt,
    String resultingState) {}
