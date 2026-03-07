package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.response.HealthSnapshotResponse;
import com.example.opsmonitor.api.dto.response.ServiceStatusHistoryItemResponse;
import com.example.opsmonitor.api.dto.response.ServiceStatusResponse;
import com.example.opsmonitor.application.support.DtoMapper;
import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.infrastructure.cache.StatusCacheService;
import com.example.opsmonitor.infrastructure.repository.HealthSnapshotRepository;
import com.example.opsmonitor.infrastructure.repository.ServiceStatusHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceStatusQueryService {

  private final MonitoredServiceService monitoredServiceService;
  private final ServiceStatusHistoryRepository serviceStatusHistoryRepository;
  private final HealthSnapshotRepository healthSnapshotRepository;
  private final StatusCacheService statusCacheService;

  public ServiceStatusQueryService(
      MonitoredServiceService monitoredServiceService,
      ServiceStatusHistoryRepository serviceStatusHistoryRepository,
      HealthSnapshotRepository healthSnapshotRepository,
      StatusCacheService statusCacheService) {
    this.monitoredServiceService = monitoredServiceService;
    this.serviceStatusHistoryRepository = serviceStatusHistoryRepository;
    this.healthSnapshotRepository = healthSnapshotRepository;
    this.statusCacheService = statusCacheService;
  }

  @Transactional(readOnly = true)
  public ServiceStatusResponse getServiceStatus(UUID serviceId) {
    return statusCacheService
        .getServiceStatus(serviceId)
        .orElseGet(() -> computeServiceStatus(serviceId));
  }

  private ServiceStatusResponse computeServiceStatus(UUID serviceId) {
    MonitoredService service = monitoredServiceService.getEntity(serviceId);

    List<ServiceStatusHistoryItemResponse> history =
        serviceStatusHistoryRepository.findTop10ByServiceIdOrderByChangedAtDesc(serviceId).stream()
            .map(DtoMapper::toResponse)
            .toList();

    List<HealthSnapshotResponse> snapshots =
        healthSnapshotRepository.findTop5ByServiceIdOrderByRecordedAtDesc(serviceId).stream()
            .map(DtoMapper::toResponse)
            .toList();

    ServiceStatusResponse response =
        new ServiceStatusResponse(
            service.getId(),
            service.getName(),
            service.getEnvironment(),
            service.getCurrentStatus(),
            service.getLastSnapshotAt(),
            history,
            snapshots,
            false);

    statusCacheService.putServiceStatus(serviceId, response);
    return response;
  }
}
