package com.example.opsmonitor.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMonitoredServiceRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 40) @Pattern(regexp = "[a-zA-Z0-9_-]+") String environment,
    @NotBlank @Size(max = 80) String ownerTeam,
    @Size(max = 255) String endpointUrl) {}
