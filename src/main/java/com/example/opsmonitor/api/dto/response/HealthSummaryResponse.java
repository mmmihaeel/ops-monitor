package com.example.opsmonitor.api.dto.response;

import java.time.Instant;

public record HealthSummaryResponse(
    String applicationStatus,
    long totalServices,
    long servicesUp,
    long servicesDegraded,
    long servicesDown,
    long servicesUnknown,
    long activeFailedJobs,
    long activeIncidents,
    long incidentNotesLast24h,
    Instant generatedAt,
    boolean cached) {}
