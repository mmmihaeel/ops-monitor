package com.example.opsmonitor.application.service;

import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.infrastructure.config.StatusPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class ServiceStatusPolicy {

  private final StatusPolicyProperties statusPolicyProperties;

  public ServiceStatusPolicy(StatusPolicyProperties statusPolicyProperties) {
    this.statusPolicyProperties = statusPolicyProperties;
  }

  public ServiceStatus deriveStatus(
      ServiceStatus reportedStatus, Integer latencyMs, String errorMessage) {
    ServiceStatus derived = reportedStatus;

    if (errorMessage != null && !errorMessage.isBlank()) {
      derived = maxSeverity(derived, ServiceStatus.DEGRADED);
    }

    if (latencyMs != null) {
      if (latencyMs >= statusPolicyProperties.getDownLatencyThresholdMs()) {
        derived = maxSeverity(derived, ServiceStatus.DOWN);
      } else if (latencyMs >= statusPolicyProperties.getDegradedLatencyThresholdMs()) {
        derived = maxSeverity(derived, ServiceStatus.DEGRADED);
      }
    }

    return derived;
  }

  public String deriveApplicationStatus(
      long totalServices, long servicesDown, long servicesDegraded) {
    if (totalServices == 0) {
      return "UNKNOWN";
    }
    if (servicesDown > 0) {
      return "DOWN";
    }
    if (servicesDegraded > 0) {
      return "DEGRADED";
    }
    return "UP";
  }

  private ServiceStatus maxSeverity(ServiceStatus a, ServiceStatus b) {
    return severityScore(a) >= severityScore(b) ? a : b;
  }

  private int severityScore(ServiceStatus status) {
    return switch (status) {
      case UNKNOWN -> 0;
      case UP -> 1;
      case DEGRADED -> 2;
      case DOWN -> 3;
    };
  }
}
