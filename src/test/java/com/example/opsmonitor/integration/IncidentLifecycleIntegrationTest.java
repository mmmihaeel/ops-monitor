package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.AcknowledgeIncidentRequest;
import com.example.opsmonitor.api.dto.request.CreateIncidentNoteRequest;
import com.example.opsmonitor.api.dto.request.ResolveIncidentRequest;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class IncidentLifecycleIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldSupportOpenAcknowledgeResolveLifecycle() throws Exception {
    CreateIncidentNoteRequest createRequest =
        new CreateIncidentNoteRequest(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            UUID.fromString("44444444-1111-1111-1111-111111111111"),
            IncidentSeverity.CRITICAL,
            "Order retries degraded",
            "Retries are delayed by dependency timeout",
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
                    .andExpect(jsonPath("$.data.status", is("OPEN")))
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .path("data")
            .path("id")
            .asText();

    AcknowledgeIncidentRequest acknowledgeRequest =
        new AcknowledgeIncidentRequest("ops.operator", "Taking ownership");
    mockMvc
        .perform(
            post("/api/v1/incidents/{id}/acknowledge", incidentId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acknowledgeRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("ACKNOWLEDGED")))
        .andExpect(jsonPath("$.data.acknowledgedBy", is("ops.operator")));

    ResolveIncidentRequest resolveRequest =
        new ResolveIncidentRequest("ops.operator", "Dependency timeout mitigated");
    mockMvc
        .perform(
            post("/api/v1/incidents/{id}/resolve", incidentId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resolveRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("RESOLVED")))
        .andExpect(jsonPath("$.data.resolvedBy", is("ops.operator")));

    mockMvc
        .perform(get("/api/v1/incidents/{id}", incidentId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("RESOLVED")));

    mockMvc
        .perform(
            get("/api/v1/audit-entries")
                .with(adminAuth())
                .param("action", "INCIDENT_ACKNOWLEDGED")
                .param("size", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

    mockMvc
        .perform(
            get("/api/v1/audit-entries")
                .with(adminAuth())
                .param("action", "INCIDENT_RESOLVED")
                .param("size", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
  }

  @Test
  void shouldRejectResolveWhenIncidentNotAcknowledged() throws Exception {
    String incidentId = "66666666-1111-1111-1111-111111111111";
    ResolveIncidentRequest resolveRequest =
        new ResolveIncidentRequest("ops.operator", "Attempting invalid resolve");

    mockMvc
        .perform(
            post("/api/v1/incidents/{id}/resolve", incidentId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resolveRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }

  @Test
  void shouldRejectAcknowledgeWhenIncidentAlreadyAcknowledged() throws Exception {
    String incidentId = "66666666-2222-2222-2222-222222222222";
    AcknowledgeIncidentRequest request =
        new AcknowledgeIncidentRequest("ops.operator", "Attempting duplicate acknowledge");

    mockMvc
        .perform(
            post("/api/v1/incidents/{id}/acknowledge", incidentId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }
}
