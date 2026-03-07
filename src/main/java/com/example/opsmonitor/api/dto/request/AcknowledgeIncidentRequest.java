package com.example.opsmonitor.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcknowledgeIncidentRequest(
    @NotBlank @Size(max = 80) String actor, @Size(max = 500) String comment) {}
