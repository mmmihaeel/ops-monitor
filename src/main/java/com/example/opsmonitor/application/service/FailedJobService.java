package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.request.RetryFailedJobRequest;
import com.example.opsmonitor.api.dto.response.FailedJobDetailResponse;
import com.example.opsmonitor.api.dto.response.FailedJobResponse;
import com.example.opsmonitor.api.dto.response.RetryFailedJobResultResponse;
import com.example.opsmonitor.application.support.ConflictException;
import com.example.opsmonitor.application.support.DtoMapper;
import com.example.opsmonitor.application.support.NotFoundException;
import com.example.opsmonitor.domain.model.FailedJob;
import com.example.opsmonitor.domain.model.RetryAttempt;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import com.example.opsmonitor.domain.model.enums.FailedJobState;
import com.example.opsmonitor.domain.model.enums.RetryOutcome;
import com.example.opsmonitor.infrastructure.cache.RetryLockService;
import com.example.opsmonitor.infrastructure.cache.StatusCacheService;
import com.example.opsmonitor.infrastructure.config.RetrySettingsProperties;
import com.example.opsmonitor.infrastructure.repository.FailedJobRepository;
import com.example.opsmonitor.infrastructure.repository.RetryAttemptRepository;
import com.example.opsmonitor.infrastructure.repository.specification.FailedJobSpecifications;
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
public class FailedJobService {

  private final FailedJobRepository failedJobRepository;
  private final RetryAttemptRepository retryAttemptRepository;
  private final RetryLockService retryLockService;
  private final RetryBackoffPolicy retryBackoffPolicy;
  private final RetrySettingsProperties retrySettingsProperties;
  private final AuditEntryService auditEntryService;
  private final StatusCacheService statusCacheService;
  private final Clock clock;

  public FailedJobService(
      FailedJobRepository failedJobRepository,
      RetryAttemptRepository retryAttemptRepository,
      RetryLockService retryLockService,
      RetryBackoffPolicy retryBackoffPolicy,
      RetrySettingsProperties retrySettingsProperties,
      AuditEntryService auditEntryService,
      StatusCacheService statusCacheService,
      Clock clock) {
    this.failedJobRepository = failedJobRepository;
    this.retryAttemptRepository = retryAttemptRepository;
    this.retryLockService = retryLockService;
    this.retryBackoffPolicy = retryBackoffPolicy;
    this.retrySettingsProperties = retrySettingsProperties;
    this.auditEntryService = auditEntryService;
    this.statusCacheService = statusCacheService;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public Page<FailedJobResponse> list(
      UUID serviceId, FailedJobState state, String jobType, Pageable pageable) {
    Specification<FailedJob> specification =
        Specification.where(FailedJobSpecifications.serviceIdEquals(serviceId))
            .and(FailedJobSpecifications.stateEquals(state))
            .and(FailedJobSpecifications.jobTypeEquals(jobType));
    return failedJobRepository.findAll(specification, pageable).map(DtoMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public FailedJobDetailResponse get(UUID failedJobId) {
    FailedJob failedJob = getEntity(failedJobId);
    FailedJobResponse failedJobResponse = DtoMapper.toResponse(failedJob);
    var attempts =
        retryAttemptRepository.findByFailedJobIdOrderByTriggeredAtDesc(failedJobId).stream()
            .map(attempt -> DtoMapper.toResponse(attempt, failedJob.getState().name()))
            .toList();
    return new FailedJobDetailResponse(failedJobResponse, attempts);
  }

  @Transactional
  public RetryFailedJobResultResponse retry(UUID failedJobId, RetryFailedJobRequest request) {
    FailedJob failedJob = getEntity(failedJobId);

    boolean lockAcquired =
        retryLockService.acquire(failedJobId, retrySettingsProperties.getLockTtl());
    if (!lockAcquired) {
      throw new ConflictException("Retry already in progress for failed job " + failedJobId);
    }

    try {
      if (!failedJob.isRetryable()) {
        throw new ConflictException("Failed job is not retryable in current state");
      }

      int attemptNumber = failedJob.getRetryCount() + 1;
      Instant now = Instant.now(clock);

      failedJob.setState(FailedJobState.RETRY_IN_PROGRESS);
      failedJob.setRetryCount(attemptNumber);
      failedJob.setNextRetryAt(null);

      RetryOutcome outcome = request.outcome();
      if (outcome == RetryOutcome.SUCCESS) {
        failedJob.setState(FailedJobState.RECOVERED);
      } else if (attemptNumber >= failedJob.getMaxRetries()) {
        failedJob.setState(FailedJobState.EXHAUSTED);
      } else {
        failedJob.setState(FailedJobState.RETRY_SCHEDULED);
        failedJob.setNextRetryAt(now.plus(retryBackoffPolicy.nextDelay(attemptNumber)));
      }

      FailedJob updated = failedJobRepository.save(failedJob);

      RetryAttempt retryAttempt = new RetryAttempt();
      retryAttempt.setFailedJob(updated);
      retryAttempt.setAttemptNumber(attemptNumber);
      retryAttempt.setRequestedBy(request.requestedBy());
      retryAttempt.setOutcome(outcome);
      retryAttempt.setMessage(request.message());
      retryAttempt.setTriggeredAt(now);
      RetryAttempt savedAttempt = retryAttemptRepository.save(retryAttempt);

      Map<String, Object> auditDetails = new HashMap<>();
      auditDetails.put("attempt", attemptNumber);
      auditDetails.put("outcome", outcome);
      auditDetails.put("resultingState", updated.getState());
      auditDetails.put("nextRetryAt", updated.getNextRetryAt());

      auditEntryService.record(
          "FailedJob",
          updated.getId().toString(),
          AuditAction.FAILED_JOB_RETRY_REQUESTED,
          request.requestedBy(),
          auditDetails);

      statusCacheService.evictGlobalSummary();
      statusCacheService.evictServiceStatus(updated.getService().getId());

      return new RetryFailedJobResultResponse(
          DtoMapper.toResponse(updated),
          DtoMapper.toResponse(savedAttempt, updated.getState().name()));
    } finally {
      retryLockService.release(failedJobId);
    }
  }

  private FailedJob getEntity(UUID failedJobId) {
    return failedJobRepository
        .findById(failedJobId)
        .orElseThrow(() -> new NotFoundException("Failed job not found: " + failedJobId));
  }
}
