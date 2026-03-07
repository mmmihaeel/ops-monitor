package com.example.opsmonitor.application.support;

import com.example.opsmonitor.api.dto.response.AuditEntryResponse;
import com.example.opsmonitor.api.dto.response.FailedJobResponse;
import com.example.opsmonitor.api.dto.response.HealthSnapshotResponse;
import com.example.opsmonitor.api.dto.response.IncidentNoteResponse;
import com.example.opsmonitor.api.dto.response.MonitoredServiceResponse;
import com.example.opsmonitor.api.dto.response.RetryAttemptResponse;
import com.example.opsmonitor.api.dto.response.ServiceStatusHistoryItemResponse;
import com.example.opsmonitor.domain.model.AuditEntry;
import com.example.opsmonitor.domain.model.FailedJob;
import com.example.opsmonitor.domain.model.HealthSnapshot;
import com.example.opsmonitor.domain.model.IncidentNote;
import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.domain.model.RetryAttempt;
import com.example.opsmonitor.domain.model.ServiceStatusHistory;

public final class DtoMapper {

  private DtoMapper() {}

  public static MonitoredServiceResponse toResponse(MonitoredService service) {
    return new MonitoredServiceResponse(
        service.getId(),
        service.getName(),
        service.getEnvironment(),
        service.getOwnerTeam(),
        service.getEndpointUrl(),
        service.getCurrentStatus(),
        service.getLastSnapshotAt(),
        service.getCreatedAt(),
        service.getUpdatedAt());
  }

  public static HealthSnapshotResponse toResponse(HealthSnapshot snapshot) {
    return new HealthSnapshotResponse(
        snapshot.getId(),
        snapshot.getService().getId(),
        snapshot.getStatus(),
        snapshot.getLatencyMs(),
        snapshot.getErrorMessage(),
        snapshot.getSource(),
        snapshot.getRecordedAt());
  }

  public static ServiceStatusHistoryItemResponse toResponse(ServiceStatusHistory history) {
    return new ServiceStatusHistoryItemResponse(
        history.getPreviousStatus(),
        history.getNewStatus(),
        history.getReason(),
        history.getChangedAt());
  }

  public static FailedJobResponse toResponse(FailedJob failedJob) {
    return new FailedJobResponse(
        failedJob.getId(),
        failedJob.getService().getId(),
        failedJob.getService().getName(),
        failedJob.getExternalJobId(),
        failedJob.getJobType(),
        failedJob.getState(),
        failedJob.getFailureReason(),
        failedJob.getPayload(),
        failedJob.getRetryCount(),
        failedJob.getMaxRetries(),
        failedJob.getLastFailureAt(),
        failedJob.getNextRetryAt(),
        failedJob.getCreatedAt(),
        failedJob.getUpdatedAt());
  }

  public static RetryAttemptResponse toResponse(RetryAttempt retryAttempt, String resultingState) {
    return new RetryAttemptResponse(
        retryAttempt.getId(),
        retryAttempt.getFailedJob().getId(),
        retryAttempt.getAttemptNumber(),
        retryAttempt.getRequestedBy(),
        retryAttempt.getOutcome(),
        retryAttempt.getMessage(),
        retryAttempt.getTriggeredAt(),
        resultingState);
  }

  public static IncidentNoteResponse toResponse(IncidentNote incidentNote) {
    return new IncidentNoteResponse(
        incidentNote.getId(),
        incidentNote.getService() != null ? incidentNote.getService().getId() : null,
        incidentNote.getFailedJob() != null ? incidentNote.getFailedJob().getId() : null,
        incidentNote.getSeverity(),
        incidentNote.getStatus(),
        incidentNote.getTitle(),
        incidentNote.getNote(),
        incidentNote.getAuthor(),
        incidentNote.getAcknowledgedAt(),
        incidentNote.getAcknowledgedBy(),
        incidentNote.getResolvedAt(),
        incidentNote.getResolvedBy(),
        incidentNote.getCreatedAt());
  }

  public static AuditEntryResponse toResponse(AuditEntry auditEntry) {
    return new AuditEntryResponse(
        auditEntry.getId(),
        auditEntry.getEntityType(),
        auditEntry.getEntityId(),
        auditEntry.getAction(),
        auditEntry.getActor(),
        auditEntry.getDetailsJson(),
        auditEntry.getCreatedAt());
  }
}
