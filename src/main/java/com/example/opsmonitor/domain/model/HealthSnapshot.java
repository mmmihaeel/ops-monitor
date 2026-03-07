package com.example.opsmonitor.domain.model;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.domain.model.enums.SnapshotSource;
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
@Table(name = "health_snapshot")
public class HealthSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private MonitoredService service;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ServiceStatus status;

  @Column(name = "latency_ms")
  private Integer latencyMs;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SnapshotSource source;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  @PrePersist
  void prePersist() {
    if (recordedAt == null) {
      recordedAt = Instant.now();
    }
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

  public ServiceStatus getStatus() {
    return status;
  }

  public void setStatus(ServiceStatus status) {
    this.status = status;
  }

  public Integer getLatencyMs() {
    return latencyMs;
  }

  public void setLatencyMs(Integer latencyMs) {
    this.latencyMs = latencyMs;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public SnapshotSource getSource() {
    return source;
  }

  public void setSource(SnapshotSource source) {
    this.source = source;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public void setRecordedAt(Instant recordedAt) {
    this.recordedAt = recordedAt;
  }
}
