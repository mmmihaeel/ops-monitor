package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.CreateHealthSnapshotRequest;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.domain.model.enums.SnapshotSource;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class HealthSnapshotIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldRecordSnapshotAndUpdateServiceStatus() throws Exception {
    UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId, ServiceStatus.UP, 55, null, SnapshotSource.API, null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.serviceId", is(serviceId.toString())))
        .andExpect(jsonPath("$.data.status", is("UP")));

    mockMvc
        .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentStatus", is("UP")))
        .andExpect(jsonPath("$.data.history.length()", greaterThanOrEqualTo(1)));
  }

  @Test
  void shouldDeriveDegradedStatusWhenLatencyCrossesThreshold() throws Exception {
    UUID serviceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId, ServiceStatus.UP, 2500, null, SnapshotSource.PROBE, null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("DEGRADED")));

    mockMvc
        .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentStatus", is("DEGRADED")));
  }

  @Test
  void shouldDeriveDownStatusWhenLatencyCrossesDownThreshold() throws Exception {
    UUID serviceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId, ServiceStatus.UP, 7000, null, SnapshotSource.PROBE, null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("DOWN")));

    mockMvc
        .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentStatus", is("DOWN")));
  }

  @Test
  void shouldDeriveDegradedStatusWhenErrorMessageIsProvided() throws Exception {
    UUID serviceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId, ServiceStatus.UP, 100, "Dependency timeout", SnapshotSource.API, null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("DEGRADED")));
  }
}
