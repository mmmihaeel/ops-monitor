package com.example.opsmonitor.domain.model;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
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

@Entity
@Table(name = "service_status_history")
public class ServiceStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private MonitoredService service;

  @Enumerated(EnumType.STRING)
  @Column(name = "previous_status", nullable = false, length = 20)
  private ServiceStatus previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false, length = 20)
  private ServiceStatus newStatus;

  @Column(nullable = false, length = 200)
  private String reason;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;

  @PrePersist
  void prePersist() {
    if (changedAt == null) {
      changedAt = Instant.now();
    }
  }

  public Long getId() {
    return id;
  }

  public MonitoredService getService() {
    return service;
  }

  public void setService(MonitoredService service) {
    this.service = service;
  }

  public ServiceStatus getPreviousStatus() {
    return previousStatus;
  }

  public void setPreviousStatus(ServiceStatus previousStatus) {
    this.previousStatus = previousStatus;
  }

  public ServiceStatus getNewStatus() {
    return newStatus;
  }

  public void setNewStatus(ServiceStatus newStatus) {
    this.newStatus = newStatus;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Instant getChangedAt() {
    return changedAt;
  }

  public void setChangedAt(Instant changedAt) {
    this.changedAt = changedAt;
  }
}
