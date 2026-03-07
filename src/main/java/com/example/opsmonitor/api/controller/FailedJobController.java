package com.example.opsmonitor.api.controller;

import com.example.opsmonitor.api.dto.request.RetryFailedJobRequest;
import com.example.opsmonitor.api.dto.response.FailedJobDetailResponse;
import com.example.opsmonitor.api.dto.response.FailedJobResponse;
import com.example.opsmonitor.api.dto.response.RetryFailedJobResultResponse;
import com.example.opsmonitor.api.support.ApiResponse;
import com.example.opsmonitor.api.support.ApiResponseFactory;
import com.example.opsmonitor.application.service.FailedJobService;
import com.example.opsmonitor.application.support.PaginationSupport;
import com.example.opsmonitor.domain.model.enums.FailedJobState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/failed-jobs")
@Validated
public class FailedJobController {

  private static final Set<String> SORT_FIELDS =
      Set.of("lastFailureAt", "retryCount", "state", "createdAt");

  private final FailedJobService failedJobService;

  public FailedJobController(FailedJobService failedJobService) {
    this.failedJobService = failedJobService;
  }

  @GetMapping
  public ApiResponse<Iterable<FailedJobResponse>> list(
      @RequestParam(required = false) UUID serviceId,
      @RequestParam(required = false) FailedJobState state,
      @RequestParam(required = false) String jobType,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(defaultValue = "lastFailureAt,desc") String sort) {
    var pageable = PaginationSupport.pageable(page, size, sort, SORT_FIELDS, "lastFailureAt");
    var result = failedJobService.list(serviceId, state, jobType, pageable);
    return ApiResponseFactory.page(result);
  }

  @GetMapping("/{failedJobId}")
  public ApiResponse<FailedJobDetailResponse> get(@PathVariable UUID failedJobId) {
    return ApiResponseFactory.ok(failedJobService.get(failedJobId));
  }

  @PostMapping("/{failedJobId}/retry")
  public ApiResponse<RetryFailedJobResultResponse> retry(
      @PathVariable UUID failedJobId, @Valid @RequestBody RetryFailedJobRequest request) {
    return ApiResponseFactory.ok(failedJobService.retry(failedJobId, request));
  }
}
