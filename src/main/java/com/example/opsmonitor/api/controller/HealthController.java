package com.example.opsmonitor.api.controller;

import com.example.opsmonitor.api.dto.response.HealthSummaryResponse;
import com.example.opsmonitor.api.dto.response.ReadinessResponse;
import com.example.opsmonitor.api.support.ApiResponse;
import com.example.opsmonitor.api.support.ApiResponseFactory;
import com.example.opsmonitor.application.service.HealthSummaryService;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  private final HealthSummaryService healthSummaryService;

  public HealthController(HealthSummaryService healthSummaryService) {
    this.healthSummaryService = healthSummaryService;
  }

  @GetMapping
  public ApiResponse<HealthSummaryResponse> getHealth() {
    return ApiResponseFactory.ok(healthSummaryService.getSummary());
  }

  @GetMapping("/readiness")
  public ApiResponse<ReadinessResponse> getReadiness() {
    return ApiResponseFactory.ok(new ReadinessResponse("READY", Instant.now()));
  }
}
