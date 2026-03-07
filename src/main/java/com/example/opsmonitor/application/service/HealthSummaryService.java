package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.response.HealthSummaryResponse;
import com.example.opsmonitor.domain.model.enums.FailedJobState;
import com.example.opsmonitor.domain.model.enums.IncidentStatus;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.infrastructure.cache.StatusCacheService;
import com.example.opsmonitor.infrastructure.repository.FailedJobRepository;
import com.example.opsmonitor.infrastructure.repository.IncidentNoteRepository;
import com.example.opsmonitor.infrastructure.repository.MonitoredServiceRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthSummaryService {

  private final MonitoredServiceRepository monitoredServiceRepository;
  private final FailedJobRepository failedJobRepository;
  private final IncidentNoteRepository incidentNoteRepository;
  private final StatusCacheService statusCacheService;
  private final ServiceStatusPolicy serviceStatusPolicy;
  private final Clock clock;

  public HealthSummaryService(
      MonitoredServiceRepository monitoredServiceRepository,
      FailedJobRepository failedJobRepository,
      IncidentNoteRepository incidentNoteRepository,
      StatusCacheService statusCacheService,
      ServiceStatusPolicy serviceStatusPolicy,
      Clock clock) {
    this.monitoredServiceRepository = monitoredServiceRepository;
    this.failedJobRepository = failedJobRepository;
    this.incidentNoteRepository = incidentNoteRepository;
    this.statusCacheService = statusCacheService;
    this.serviceStatusPolicy = serviceStatusPolicy;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public HealthSummaryResponse getSummary() {
    return statusCacheService.getGlobalSummary().orElseGet(this::computeSummary);
  }

  private HealthSummaryResponse computeSummary() {
    long totalServices = monitoredServiceRepository.count();
    long servicesUp = monitoredServiceRepository.countByCurrentStatus(ServiceStatus.UP);
    long servicesDegraded = monitoredServiceRepository.countByCurrentStatus(ServiceStatus.DEGRADED);
    long servicesDown = monitoredServiceRepository.countByCurrentStatus(ServiceStatus.DOWN);
    long servicesUnknown = monitoredServiceRepository.countByCurrentStatus(ServiceStatus.UNKNOWN);

    long activeFailedJobs =
        failedJobRepository.countByStateIn(
            List.of(
                FailedJobState.FAILED,
                FailedJobState.RETRY_IN_PROGRESS,
                FailedJobState.RETRY_SCHEDULED,
                FailedJobState.EXHAUSTED));

    Instant now = Instant.now(clock);
    long incidentNotesLast24h =
        incidentNoteRepository.countByCreatedAtAfter(now.minusSeconds(86_400));
    long activeIncidents =
        incidentNoteRepository.countByStatusIn(
            List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED));

    String applicationStatus =
        serviceStatusPolicy.deriveApplicationStatus(totalServices, servicesDown, servicesDegraded);

    HealthSummaryResponse response =
        new HealthSummaryResponse(
            applicationStatus,
            totalServices,
            servicesUp,
            servicesDegraded,
            servicesDown,
            servicesUnknown,
            activeFailedJobs,
            activeIncidents,
            incidentNotesLast24h,
            now,
            false);

    statusCacheService.putGlobalSummary(response);
    return response;
  }
}
