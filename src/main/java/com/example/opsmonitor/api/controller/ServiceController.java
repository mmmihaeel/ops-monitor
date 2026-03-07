package com.example.opsmonitor.api.controller;

import com.example.opsmonitor.api.dto.request.CreateMonitoredServiceRequest;
import com.example.opsmonitor.api.dto.response.MonitoredServiceResponse;
import com.example.opsmonitor.api.dto.response.ServiceStatusResponse;
import com.example.opsmonitor.api.support.ApiResponse;
import com.example.opsmonitor.api.support.ApiResponseFactory;
import com.example.opsmonitor.application.service.MonitoredServiceService;
import com.example.opsmonitor.application.service.ServiceStatusQueryService;
import com.example.opsmonitor.application.support.PaginationSupport;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
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
@RequestMapping("/api/v1/services")
@Validated
public class ServiceController {

  private static final Set<String> SORT_FIELDS =
      Set.of("name", "environment", "currentStatus", "createdAt", "updatedAt");

  private final MonitoredServiceService monitoredServiceService;
  private final ServiceStatusQueryService serviceStatusQueryService;

  public ServiceController(
      MonitoredServiceService monitoredServiceService,
      ServiceStatusQueryService serviceStatusQueryService) {
    this.monitoredServiceService = monitoredServiceService;
    this.serviceStatusQueryService = serviceStatusQueryService;
  }

  @GetMapping
  public ApiResponse<Iterable<MonitoredServiceResponse>> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String environment,
      @RequestParam(required = false) ServiceStatus status,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(defaultValue = "createdAt,desc") String sort) {
    var pageable = PaginationSupport.pageable(page, size, sort, SORT_FIELDS, "createdAt");
    var result = monitoredServiceService.list(q, environment, status, pageable);
    return ApiResponseFactory.page(result);
  }

  @PostMapping
  public ApiResponse<MonitoredServiceResponse> create(
      @Valid @RequestBody CreateMonitoredServiceRequest request) {
    return ApiResponseFactory.ok(monitoredServiceService.create(request));
  }

  @GetMapping("/{serviceId}")
  public ApiResponse<MonitoredServiceResponse> get(@PathVariable UUID serviceId) {
    return ApiResponseFactory.ok(monitoredServiceService.get(serviceId));
  }

  @GetMapping("/{serviceId}/status")
  public ApiResponse<ServiceStatusResponse> getStatus(@PathVariable UUID serviceId) {
    return ApiResponseFactory.ok(serviceStatusQueryService.getServiceStatus(serviceId));
  }
}
