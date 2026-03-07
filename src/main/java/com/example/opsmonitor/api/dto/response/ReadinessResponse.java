package com.example.opsmonitor.api.dto.response;

import java.time.Instant;

public record ReadinessResponse(String status, Instant checkedAt) {}
