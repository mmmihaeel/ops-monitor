package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.request.CreateHealthSnapshotRequest;
import com.example.opsmonitor.api.dto.response.HealthSnapshotResponse;
import com.example.opsmonitor.application.support.DtoMapper;
import com.example.opsmonitor.domain.model.HealthSnapshot;
import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.domain.model.ServiceStatusHistory;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.infrastructure.cache.StatusCacheService;
import com.example.opsmonitor.infrastructure.repository.HealthSnapshotRepository;
import com.example.opsmonitor.infrastructure.repository.MonitoredServiceRepository;
import com.example.opsmonitor.infrastructure.repository.ServiceStatusHistoryRepository;
import com.example.opsmonitor.infrastructure.repository.specification.HealthSnapshotSpecifications;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthSnapshotService {

  private final HealthSnapshotRepository healthSnapshotRepository;
  private final ServiceStatusHistoryRepository serviceStatusHistoryRepository;
  private final MonitoredServiceRepository monitoredServiceRepository;
  private final MonitoredServiceService monitoredServiceService;
  private final AuditEntryService auditEntryService;
  private final StatusCacheService statusCacheService;
  private final ServiceStatusPolicy serviceStatusPolicy;
  private final Clock clock;

  public HealthSnapshotService(
      HealthSnapshotRepository healthSnapshotRepository,
      ServiceStatusHistoryRepository serviceStatusHistoryRepository,
      MonitoredServiceRepository monitoredServiceRepository,
      MonitoredServiceService monitoredServiceService,
      AuditEntryService auditEntryService,
      StatusCacheService statusCacheService,
      ServiceStatusPolicy serviceStatusPolicy,
      Clock clock) {
    this.healthSnapshotRepository = healthSnapshotRepository;
    this.serviceStatusHistoryRepository = serviceStatusHistoryRepository;
    this.monitoredServiceRepository = monitoredServiceRepository;
    this.monitoredServiceService = monitoredServiceService;
    this.auditEntryService = auditEntryService;
    this.statusCacheService = statusCacheService;
    this.serviceStatusPolicy = serviceStatusPolicy;
    this.clock = clock;
  }

  @Transactional
  public HealthSnapshotResponse create(CreateHealthSnapshotRequest request) {
    MonitoredService service = monitoredServiceService.getEntity(request.serviceId());

    HealthSnapshot snapshot = new HealthSnapshot();
    snapshot.setService(service);
    ServiceStatus effectiveStatus =
        serviceStatusPolicy.deriveStatus(
            request.status(), request.latencyMs(), request.errorMessage());
    snapshot.setStatus(effectiveStatus);
    snapshot.setLatencyMs(request.latencyMs());
    snapshot.setErrorMessage(request.errorMessage());
    snapshot.setSource(request.source());
    snapshot.setRecordedAt(
        request.recordedAt() == null ? Instant.now(clock) : request.recordedAt());

    HealthSnapshot savedSnapshot = healthSnapshotRepository.save(snapshot);

    ServiceStatus previousStatus = service.getCurrentStatus();
    service.setCurrentStatus(effectiveStatus);
    service.setLastSnapshotAt(savedSnapshot.getRecordedAt());
    monitoredServiceRepository.save(service);

    if (previousStatus != effectiveStatus) {
      ServiceStatusHistory statusHistory = new ServiceStatusHistory();
      statusHistory.setService(service);
      statusHistory.setPreviousStatus(previousStatus);
      statusHistory.setNewStatus(effectiveStatus);
      statusHistory.setReason("Health snapshot recorded via " + request.source());
      statusHistory.setChangedAt(savedSnapshot.getRecordedAt());
      serviceStatusHistoryRepository.save(statusHistory);

      auditEntryService.record(
          "MonitoredService",
          service.getId().toString(),
          AuditAction.SERVICE_STATUS_CHANGED,
          "api",
          Map.of(
              "from", previousStatus,
              "to", effectiveStatus,
              "reason", statusHistory.getReason()));
    }

    auditEntryService.record(
        "HealthSnapshot",
        savedSnapshot.getId().toString(),
        AuditAction.HEALTH_SNAPSHOT_RECORDED,
        "api",
        Map.of(
            "serviceId", service.getId(),
            "reportedStatus", request.status(),
            "status", savedSnapshot.getStatus(),
            "source", savedSnapshot.getSource(),
            "recordedAt", savedSnapshot.getRecordedAt()));

    statusCacheService.evictServiceStatus(service.getId());
    statusCacheService.evictGlobalSummary();

    return DtoMapper.toResponse(savedSnapshot);
  }

  @Transactional(readOnly = true)
  public Page<HealthSnapshotResponse> list(
      java.util.UUID serviceId, ServiceStatus status, Instant from, Instant to, Pageable pageable) {
    Specification<HealthSnapshot> specification =
        Specification.where(HealthSnapshotSpecifications.serviceIdEquals(serviceId))
            .and(HealthSnapshotSpecifications.statusEquals(status))
            .and(HealthSnapshotSpecifications.recordedAtFrom(from))
            .and(HealthSnapshotSpecifications.recordedAtTo(to));

    return healthSnapshotRepository.findAll(specification, pageable).map(DtoMapper::toResponse);
  }
}
