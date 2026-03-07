package com.example.opsmonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.opsmonitor.application.service.ServiceStatusPolicy;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.infrastructure.config.StatusPolicyProperties;
import org.junit.jupiter.api.Test;

class ServiceStatusPolicyTest {

  private final ServiceStatusPolicy serviceStatusPolicy = createPolicy(1200, 5000);

  @Test
  void shouldPromoteStatusToDegradedWhenLatencyIsHigh() {
    ServiceStatus status = serviceStatusPolicy.deriveStatus(ServiceStatus.UP, 1500, null);
    assertEquals(ServiceStatus.DEGRADED, status);
  }

  @Test
  void shouldPromoteStatusToDownWhenLatencyIsSevere() {
    ServiceStatus status = serviceStatusPolicy.deriveStatus(ServiceStatus.UP, 5200, null);
    assertEquals(ServiceStatus.DOWN, status);
  }

  @Test
  void shouldKeepExplicitDownStatus() {
    ServiceStatus status = serviceStatusPolicy.deriveStatus(ServiceStatus.DOWN, 50, null);
    assertEquals(ServiceStatus.DOWN, status);
  }

  @Test
  void shouldDeriveApplicationStatusFromServiceCounts() {
    assertEquals("UNKNOWN", serviceStatusPolicy.deriveApplicationStatus(0, 0, 0));
    assertEquals("DOWN", serviceStatusPolicy.deriveApplicationStatus(4, 1, 0));
    assertEquals("DEGRADED", serviceStatusPolicy.deriveApplicationStatus(4, 0, 2));
    assertEquals("UP", serviceStatusPolicy.deriveApplicationStatus(4, 0, 0));
  }

  private static ServiceStatusPolicy createPolicy(int degradedThreshold, int downThreshold) {
    StatusPolicyProperties properties = new StatusPolicyProperties();
    properties.setDegradedLatencyThresholdMs(degradedThreshold);
    properties.setDownLatencyThresholdMs(downThreshold);
    return new ServiceStatusPolicy(properties);
  }
}
