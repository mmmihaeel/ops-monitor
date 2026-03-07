package com.example.opsmonitor.domain.model;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "monitored_service",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_monitored_service_name_environment",
            columnNames = {"name", "environment"}))
public class MonitoredService {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 40)
  private String environment;

  @Column(name = "owner_team", nullable = false, length = 80)
  private String ownerTeam;

  @Column(name = "endpoint_url", length = 255)
  private String endpointUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "current_status", nullable = false, length = 20)
  private ServiceStatus currentStatus;

  @Column(name = "last_snapshot_at")
  private Instant lastSnapshotAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (currentStatus == null) {
      currentStatus = ServiceStatus.UNKNOWN;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getOwnerTeam() {
    return ownerTeam;
  }

  public void setOwnerTeam(String ownerTeam) {
    this.ownerTeam = ownerTeam;
  }

  public String getEndpointUrl() {
    return endpointUrl;
  }

  public void setEndpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
  }

  public ServiceStatus getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(ServiceStatus currentStatus) {
    this.currentStatus = currentStatus;
  }

  public Instant getLastSnapshotAt() {
    return lastSnapshotAt;
  }

  public void setLastSnapshotAt(Instant lastSnapshotAt) {
    this.lastSnapshotAt = lastSnapshotAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
