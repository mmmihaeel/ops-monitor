package com.example.opsmonitor.api.dto.request;

import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateIncidentNoteRequest(
    UUID serviceId,
    UUID failedJobId,
    @NotNull IncidentSeverity severity,
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 4000) String note,
    @NotBlank @Size(max = 80) String author) {}
