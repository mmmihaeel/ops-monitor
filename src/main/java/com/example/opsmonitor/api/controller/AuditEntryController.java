package com.example.opsmonitor.api.controller;

import com.example.opsmonitor.api.dto.response.AuditEntryResponse;
import com.example.opsmonitor.api.support.ApiResponse;
import com.example.opsmonitor.api.support.ApiResponseFactory;
import com.example.opsmonitor.application.service.AuditEntryService;
import com.example.opsmonitor.application.support.PaginationSupport;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-entries")
@Validated
public class AuditEntryController {

  private static final Set<String> SORT_FIELDS = Set.of("createdAt", "action", "entityType");

  private final AuditEntryService auditEntryService;

  public AuditEntryController(AuditEntryService auditEntryService) {
    this.auditEntryService = auditEntryService;
  }

  @GetMapping
  public ApiResponse<Iterable<AuditEntryResponse>> list(
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) AuditAction action,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "25") @Min(1) @Max(200) int size,
      @RequestParam(defaultValue = "createdAt,desc") String sort) {
    var pageable = PaginationSupport.pageable(page, size, sort, SORT_FIELDS, "createdAt");
    var result = auditEntryService.list(entityType, action, from, to, pageable);
    return ApiResponseFactory.page(result);
  }
}
