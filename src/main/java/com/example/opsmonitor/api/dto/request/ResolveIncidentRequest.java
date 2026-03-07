package com.example.opsmonitor.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveIncidentRequest(
    @NotBlank @Size(max = 80) String actor, @NotBlank @Size(max = 500) String resolution) {}
