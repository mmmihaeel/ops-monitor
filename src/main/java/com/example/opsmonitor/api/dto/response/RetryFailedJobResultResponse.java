package com.example.opsmonitor.api.dto.response;

public record RetryFailedJobResultResponse(
    FailedJobResponse failedJob, RetryAttemptResponse retryAttempt) {}
