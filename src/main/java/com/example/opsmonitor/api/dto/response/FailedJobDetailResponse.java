package com.example.opsmonitor.api.dto.response;

import java.util.List;

public record FailedJobDetailResponse(
    FailedJobResponse failedJob, List<RetryAttemptResponse> attempts) {}
