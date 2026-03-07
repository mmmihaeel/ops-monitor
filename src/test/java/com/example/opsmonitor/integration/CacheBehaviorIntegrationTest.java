package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.AcknowledgeIncidentRequest;
import com.example.opsmonitor.api.dto.request.CreateHealthSnapshotRequest;
import com.example.opsmonitor.api.dto.request.CreateIncidentNoteRequest;
import com.example.opsmonitor.api.dto.request.ResolveIncidentRequest;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import com.example.opsmonitor.domain.model.enums.SnapshotSource;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CacheBehaviorIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldCacheAndInvalidateGlobalHealthSummary() throws Exception {
    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(false)));

    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(true)));

    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            ServiceStatus.UP,
            80,
            null,
            SnapshotSource.API,
            null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(false)));
  }

  @Test
  void shouldCacheAndInvalidateServiceStatusView() throws Exception {
    UUID serviceId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    mockMvc
        .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(false)));

    mockMvc
        .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(true)));

    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId,
            ServiceStatus.DEGRADED,
            1600,
            "temporary latency spike",
            SnapshotSource.API,
            null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(false)));
  }

  @Test
  void shouldInvalidateGlobalSummaryWhenIncidentLifecycleChanges() throws Exception {
    CreateIncidentNoteRequest createRequest =
        new CreateIncidentNoteRequest(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            UUID.fromString("44444444-1111-1111-1111-111111111111"),
            IncidentSeverity.WARNING,
            "Retry queue lag",
            "Lag observed while waiting on dependency",
            "ops.operator");

    String incidentId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/v1/incidents")
                            .with(operatorAuth())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .path("data")
            .path("id")
            .asText();

    long activeIncidentsBefore =
        objectMapper
            .readTree(
                mockMvc
                    .perform(get("/api/v1/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cached", is(false)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .path("data")
            .path("activeIncidents")
            .asLong();

    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cached", is(true)));

    mockMvc
        .perform(
            post("/api/v1/incidents/{id}/acknowledge", incidentId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AcknowledgeIncidentRequest("ops.operator", "Taking ownership"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("ACKNOWLEDGED")));

    mockMvc
        .perform(
            post("/api/v1/incidents/{id}/resolve", incidentId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ResolveIncidentRequest(
                            "ops.operator", "Dependency latency normalized"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("RESOLVED")));

    String summaryAfterResolution =
        mockMvc
            .perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cached", is(false)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    long activeIncidentsAfter =
        objectMapper.readTree(summaryAfterResolution).path("data").path("activeIncidents").asLong();

    org.junit.jupiter.api.Assertions.assertEquals(activeIncidentsBefore - 1, activeIncidentsAfter);
  }
}
