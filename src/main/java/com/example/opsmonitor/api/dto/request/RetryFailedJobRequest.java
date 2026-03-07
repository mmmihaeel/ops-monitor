package com.example.opsmonitor.api.dto.request;

import com.example.opsmonitor.domain.model.enums.RetryOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RetryFailedJobRequest(
    @NotBlank @Size(max = 80) String requestedBy,
    @NotNull RetryOutcome outcome,
    @Size(max = 500) String message) {}
