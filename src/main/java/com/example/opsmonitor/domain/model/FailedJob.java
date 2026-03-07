package com.example.opsmonitor.domain.model;

import com.example.opsmonitor.domain.model.enums.FailedJobState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "failed_job")
public class FailedJob {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private MonitoredService service;

  @Column(name = "external_job_id", nullable = false, length = 120)
  private String externalJobId;

  @Column(name = "job_type", nullable = false, length = 80)
  private String jobType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private FailedJobState state;

  @Column(name = "failure_reason", nullable = false, length = 600)
  private String failureReason;

  @Column(columnDefinition = "text")
  private String payload;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "max_retries", nullable = false)
  private int maxRetries;

  @Column(name = "last_failure_at", nullable = false)
  private Instant lastFailureAt;

  @Column(name = "next_retry_at")
  private Instant nextRetryAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (state == null) {
      state = FailedJobState.FAILED;
    }
    if (maxRetries <= 0) {
      maxRetries = 3;
    }
    if (lastFailureAt == null) {
      lastFailureAt = now;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public boolean isRetryable() {
    return state != FailedJobState.RECOVERED
        && state != FailedJobState.EXHAUSTED
        && retryCount < maxRetries;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public MonitoredService getService() {
    return service;
  }

  public void setService(MonitoredService service) {
    this.service = service;
  }

  public String getExternalJobId() {
    return externalJobId;
  }

  public void setExternalJobId(String externalJobId) {
    this.externalJobId = externalJobId;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public FailedJobState getState() {
    return state;
  }

  public void setState(FailedJobState state) {
    this.state = state;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public Instant getLastFailureAt() {
    return lastFailureAt;
  }

  public void setLastFailureAt(Instant lastFailureAt) {
    this.lastFailureAt = lastFailureAt;
  }

  public Instant getNextRetryAt() {
    return nextRetryAt;
  }

  public void setNextRetryAt(Instant nextRetryAt) {
    this.nextRetryAt = nextRetryAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
