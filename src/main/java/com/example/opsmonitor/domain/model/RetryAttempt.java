package com.example.opsmonitor.domain.model;

import com.example.opsmonitor.domain.model.enums.RetryOutcome;
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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "retry_attempt")
public class RetryAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "failed_job_id", nullable = false)
  private FailedJob failedJob;

  @Column(name = "attempt_number", nullable = false)
  private int attemptNumber;

  @Column(name = "requested_by", nullable = false, length = 80)
  private String requestedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RetryOutcome outcome;

  @Column(length = 500)
  private String message;

  @Column(name = "triggered_at", nullable = false)
  private Instant triggeredAt;

  @PrePersist
  void prePersist() {
    if (triggeredAt == null) {
      triggeredAt = Instant.now();
    }
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public FailedJob getFailedJob() {
    return failedJob;
  }

  public void setFailedJob(FailedJob failedJob) {
    this.failedJob = failedJob;
  }

  public int getAttemptNumber() {
    return attemptNumber;
  }

  public void setAttemptNumber(int attemptNumber) {
    this.attemptNumber = attemptNumber;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public void setRequestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
  }

  public RetryOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(RetryOutcome outcome) {
    this.outcome = outcome;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Instant getTriggeredAt() {
    return triggeredAt;
  }

  public void setTriggeredAt(Instant triggeredAt) {
    this.triggeredAt = triggeredAt;
  }
}
