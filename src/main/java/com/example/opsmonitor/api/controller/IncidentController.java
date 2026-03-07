package com.example.opsmonitor.api.controller;

import com.example.opsmonitor.api.dto.request.AcknowledgeIncidentRequest;
import com.example.opsmonitor.api.dto.request.CreateIncidentNoteRequest;
import com.example.opsmonitor.api.dto.request.ResolveIncidentRequest;
import com.example.opsmonitor.api.dto.response.IncidentNoteResponse;
import com.example.opsmonitor.api.support.ApiResponse;
import com.example.opsmonitor.api.support.ApiResponseFactory;
import com.example.opsmonitor.application.service.IncidentNoteService;
import com.example.opsmonitor.application.support.PaginationSupport;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import com.example.opsmonitor.domain.model.enums.IncidentStatus;
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
@RequestMapping("/api/v1/incidents")
@Validated
public class IncidentController {

  private static final Set<String> SORT_FIELDS = Set.of("createdAt", "severity", "title");

  private final IncidentNoteService incidentNoteService;

  public IncidentController(IncidentNoteService incidentNoteService) {
    this.incidentNoteService = incidentNoteService;
  }

  @PostMapping
  public ApiResponse<IncidentNoteResponse> create(
      @Valid @RequestBody CreateIncidentNoteRequest request) {
    return ApiResponseFactory.ok(incidentNoteService.create(request));
  }

  @GetMapping("/{incidentId}")
  public ApiResponse<IncidentNoteResponse> get(@PathVariable UUID incidentId) {
    return ApiResponseFactory.ok(incidentNoteService.get(incidentId));
  }

  @PostMapping("/{incidentId}/acknowledge")
  public ApiResponse<IncidentNoteResponse> acknowledge(
      @PathVariable UUID incidentId, @Valid @RequestBody AcknowledgeIncidentRequest request) {
    return ApiResponseFactory.ok(incidentNoteService.acknowledge(incidentId, request));
  }

  @PostMapping("/{incidentId}/resolve")
  public ApiResponse<IncidentNoteResponse> resolve(
      @PathVariable UUID incidentId, @Valid @RequestBody ResolveIncidentRequest request) {
    return ApiResponseFactory.ok(incidentNoteService.resolve(incidentId, request));
  }

  @GetMapping
  public ApiResponse<Iterable<IncidentNoteResponse>> list(
      @RequestParam(required = false) UUID serviceId,
      @RequestParam(required = false) UUID failedJobId,
      @RequestParam(required = false) IncidentSeverity severity,
      @RequestParam(required = false) IncidentStatus status,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(defaultValue = "createdAt,desc") String sort) {
    var pageable = PaginationSupport.pageable(page, size, sort, SORT_FIELDS, "createdAt");
    var result = incidentNoteService.list(serviceId, failedJobId, severity, status, pageable);
    return ApiResponseFactory.page(result);
  }
}
