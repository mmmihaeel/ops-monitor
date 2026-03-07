package com.example.opsmonitor.api.controller;

import com.example.opsmonitor.api.dto.request.CreateHealthSnapshotRequest;
import com.example.opsmonitor.api.dto.response.HealthSnapshotResponse;
import com.example.opsmonitor.api.support.ApiResponse;
import com.example.opsmonitor.api.support.ApiResponseFactory;
import com.example.opsmonitor.application.service.HealthSnapshotService;
import com.example.opsmonitor.application.support.PaginationSupport;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health-snapshots")
@Validated
public class HealthSnapshotController {

  private static final Set<String> SORT_FIELDS = Set.of("recordedAt", "status", "source");

  private final HealthSnapshotService healthSnapshotService;

  public HealthSnapshotController(HealthSnapshotService healthSnapshotService) {
    this.healthSnapshotService = healthSnapshotService;
  }

  @PostMapping
  public ApiResponse<HealthSnapshotResponse> create(
      @Valid @RequestBody CreateHealthSnapshotRequest request) {
    return ApiResponseFactory.ok(healthSnapshotService.create(request));
  }

  @GetMapping
  public ApiResponse<Iterable<HealthSnapshotResponse>> list(
      @RequestParam(required = false) UUID serviceId,
      @RequestParam(required = false) ServiceStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(defaultValue = "recordedAt,desc") String sort) {
    var pageable = PaginationSupport.pageable(page, size, sort, SORT_FIELDS, "recordedAt");
    var result = healthSnapshotService.list(serviceId, status, from, to, pageable);
    return ApiResponseFactory.page(result);
  }
}
