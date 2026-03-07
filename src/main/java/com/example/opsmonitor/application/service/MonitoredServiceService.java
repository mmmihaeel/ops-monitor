package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.request.CreateMonitoredServiceRequest;
import com.example.opsmonitor.api.dto.response.MonitoredServiceResponse;
import com.example.opsmonitor.application.support.ConflictException;
import com.example.opsmonitor.application.support.DtoMapper;
import com.example.opsmonitor.application.support.NotFoundException;
import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.infrastructure.cache.StatusCacheService;
import com.example.opsmonitor.infrastructure.repository.MonitoredServiceRepository;
import com.example.opsmonitor.infrastructure.repository.specification.MonitoredServiceSpecifications;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoredServiceService {

  private final MonitoredServiceRepository monitoredServiceRepository;
  private final AuditEntryService auditEntryService;
  private final StatusCacheService statusCacheService;

  public MonitoredServiceService(
      MonitoredServiceRepository monitoredServiceRepository,
      AuditEntryService auditEntryService,
      StatusCacheService statusCacheService) {
    this.monitoredServiceRepository = monitoredServiceRepository;
    this.auditEntryService = auditEntryService;
    this.statusCacheService = statusCacheService;
  }

  @Transactional(readOnly = true)
  public Page<MonitoredServiceResponse> list(
      String query, String environment, ServiceStatus status, Pageable pageable) {
    Specification<MonitoredService> specification =
        Specification.where(MonitoredServiceSpecifications.nameContains(query))
            .and(MonitoredServiceSpecifications.environmentEquals(environment))
            .and(MonitoredServiceSpecifications.statusEquals(status));
    return monitoredServiceRepository.findAll(specification, pageable).map(DtoMapper::toResponse);
  }

  @Transactional
  public MonitoredServiceResponse create(CreateMonitoredServiceRequest request) {
    boolean exists =
        monitoredServiceRepository.existsByNameIgnoreCaseAndEnvironmentIgnoreCase(
            request.name(), request.environment());
    if (exists) {
      throw new ConflictException("Service with the same name and environment already exists");
    }

    MonitoredService monitoredService = new MonitoredService();
    monitoredService.setName(request.name().trim());
    monitoredService.setEnvironment(request.environment().trim().toLowerCase());
    monitoredService.setOwnerTeam(request.ownerTeam().trim());
    monitoredService.setEndpointUrl(
        request.endpointUrl() == null || request.endpointUrl().isBlank()
            ? null
            : request.endpointUrl().trim());

    MonitoredService saved = monitoredServiceRepository.save(monitoredService);

    auditEntryService.record(
        "MonitoredService",
        saved.getId().toString(),
        AuditAction.SERVICE_CREATED,
        "api",
        Map.of(
            "name", saved.getName(),
            "environment", saved.getEnvironment(),
            "ownerTeam", saved.getOwnerTeam()));

    statusCacheService.evictGlobalSummary();
    return DtoMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public MonitoredServiceResponse get(UUID serviceId) {
    return DtoMapper.toResponse(getEntity(serviceId));
  }

  @Transactional(readOnly = true)
  public MonitoredService getEntity(UUID serviceId) {
    return monitoredServiceRepository
        .findById(serviceId)
        .orElseThrow(() -> new NotFoundException("Service not found: " + serviceId));
  }
}
