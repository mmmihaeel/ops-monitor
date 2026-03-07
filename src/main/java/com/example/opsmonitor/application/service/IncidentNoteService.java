package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.request.AcknowledgeIncidentRequest;
import com.example.opsmonitor.api.dto.request.CreateIncidentNoteRequest;
import com.example.opsmonitor.api.dto.request.ResolveIncidentRequest;
import com.example.opsmonitor.api.dto.response.IncidentNoteResponse;
import com.example.opsmonitor.application.support.BadRequestException;
import com.example.opsmonitor.application.support.ConflictException;
import com.example.opsmonitor.application.support.DtoMapper;
import com.example.opsmonitor.application.support.NotFoundException;
import com.example.opsmonitor.domain.model.FailedJob;
import com.example.opsmonitor.domain.model.IncidentNote;
import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import com.example.opsmonitor.domain.model.enums.IncidentStatus;
import com.example.opsmonitor.infrastructure.cache.StatusCacheService;
import com.example.opsmonitor.infrastructure.repository.FailedJobRepository;
import com.example.opsmonitor.infrastructure.repository.IncidentNoteRepository;
import com.example.opsmonitor.infrastructure.repository.specification.IncidentNoteSpecifications;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentNoteService {

  private final IncidentNoteRepository incidentNoteRepository;
  private final MonitoredServiceService monitoredServiceService;
  private final FailedJobRepository failedJobRepository;
  private final AuditEntryService auditEntryService;
  private final StatusCacheService statusCacheService;
  private final Clock clock;

  public IncidentNoteService(
      IncidentNoteRepository incidentNoteRepository,
      MonitoredServiceService monitoredServiceService,
      FailedJobRepository failedJobRepository,
      AuditEntryService auditEntryService,
      StatusCacheService statusCacheService,
      Clock clock) {
    this.incidentNoteRepository = incidentNoteRepository;
    this.monitoredServiceService = monitoredServiceService;
    this.failedJobRepository = failedJobRepository;
    this.auditEntryService = auditEntryService;
    this.statusCacheService = statusCacheService;
    this.clock = clock;
  }

  @Transactional
  public IncidentNoteResponse create(CreateIncidentNoteRequest request) {
    if (request.serviceId() == null && request.failedJobId() == null) {
      throw new BadRequestException("At least one of serviceId or failedJobId must be provided");
    }

    MonitoredService service =
        request.serviceId() == null ? null : monitoredServiceService.getEntity(request.serviceId());

    FailedJob failedJob = null;
    if (request.failedJobId() != null) {
      failedJob =
          failedJobRepository
              .findById(request.failedJobId())
              .orElseThrow(
                  () -> new NotFoundException("Failed job not found: " + request.failedJobId()));
      if (service != null && !failedJob.getService().getId().equals(service.getId())) {
        throw new BadRequestException("failedJobId does not belong to the provided serviceId");
      }
      if (service == null) {
        service = failedJob.getService();
      }
    }

    IncidentNote incidentNote = new IncidentNote();
    incidentNote.setService(service);
    incidentNote.setFailedJob(failedJob);
    incidentNote.setSeverity(request.severity());
    incidentNote.setStatus(IncidentStatus.OPEN);
    incidentNote.setTitle(request.title().trim());
    incidentNote.setNote(request.note().trim());
    incidentNote.setAuthor(request.author().trim());

    IncidentNote saved = incidentNoteRepository.save(incidentNote);

    Map<String, Object> auditDetails = new HashMap<>();
    auditDetails.put("severity", saved.getSeverity());
    auditDetails.put("serviceId", service != null ? service.getId() : null);
    auditDetails.put("failedJobId", failedJob != null ? failedJob.getId() : null);
    auditDetails.put("title", saved.getTitle());
    auditDetails.put("status", saved.getStatus());

    auditEntryService.record(
        "IncidentNote",
        saved.getId().toString(),
        AuditAction.INCIDENT_NOTE_CREATED,
        saved.getAuthor(),
        auditDetails);

    statusCacheService.evictGlobalSummary();
    if (service != null) {
      statusCacheService.evictServiceStatus(service.getId());
    }

    return DtoMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public IncidentNoteResponse get(UUID incidentId) {
    return DtoMapper.toResponse(getEntity(incidentId));
  }

  @Transactional
  public IncidentNoteResponse acknowledge(UUID incidentId, AcknowledgeIncidentRequest request) {
    IncidentNote incident = getEntity(incidentId);
    if (incident.getStatus() != IncidentStatus.OPEN) {
      throw new ConflictException("Only OPEN incidents can be acknowledged");
    }

    Instant now = Instant.now(clock);
    incident.setStatus(IncidentStatus.ACKNOWLEDGED);
    incident.setAcknowledgedAt(now);
    incident.setAcknowledgedBy(request.actor().trim());

    IncidentNote saved = incidentNoteRepository.save(incident);

    Map<String, Object> details = new HashMap<>();
    details.put("fromStatus", IncidentStatus.OPEN);
    details.put("toStatus", IncidentStatus.ACKNOWLEDGED);
    details.put("comment", request.comment());
    details.put("incidentId", saved.getId());

    auditEntryService.record(
        "IncidentNote",
        saved.getId().toString(),
        AuditAction.INCIDENT_ACKNOWLEDGED,
        request.actor().trim(),
        details);

    evictStatusCaches(saved);
    return DtoMapper.toResponse(saved);
  }

  @Transactional
  public IncidentNoteResponse resolve(UUID incidentId, ResolveIncidentRequest request) {
    IncidentNote incident = getEntity(incidentId);
    if (incident.getStatus() != IncidentStatus.ACKNOWLEDGED) {
      throw new ConflictException("Only ACKNOWLEDGED incidents can be resolved");
    }

    Instant now = Instant.now(clock);
    incident.setStatus(IncidentStatus.RESOLVED);
    incident.setResolvedAt(now);
    incident.setResolvedBy(request.actor().trim());

    IncidentNote saved = incidentNoteRepository.save(incident);

    Map<String, Object> details = new HashMap<>();
    details.put("fromStatus", IncidentStatus.ACKNOWLEDGED);
    details.put("toStatus", IncidentStatus.RESOLVED);
    details.put("resolution", request.resolution().trim());
    details.put("incidentId", saved.getId());

    auditEntryService.record(
        "IncidentNote",
        saved.getId().toString(),
        AuditAction.INCIDENT_RESOLVED,
        request.actor().trim(),
        details);

    evictStatusCaches(saved);
    return DtoMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public Page<IncidentNoteResponse> list(
      UUID serviceId,
      UUID failedJobId,
      IncidentSeverity severity,
      IncidentStatus status,
      Pageable pageable) {
    Specification<IncidentNote> specification =
        Specification.where(IncidentNoteSpecifications.serviceIdEquals(serviceId))
            .and(IncidentNoteSpecifications.failedJobIdEquals(failedJobId))
            .and(IncidentNoteSpecifications.severityEquals(severity))
            .and(IncidentNoteSpecifications.statusEquals(status));
    return incidentNoteRepository.findAll(specification, pageable).map(DtoMapper::toResponse);
  }

  private IncidentNote getEntity(UUID incidentId) {
    return incidentNoteRepository
        .findById(incidentId)
        .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));
  }

  private void evictStatusCaches(IncidentNote incidentNote) {
    statusCacheService.evictGlobalSummary();
    if (incidentNote.getService() != null) {
      statusCacheService.evictServiceStatus(incidentNote.getService().getId());
    }
  }
}
