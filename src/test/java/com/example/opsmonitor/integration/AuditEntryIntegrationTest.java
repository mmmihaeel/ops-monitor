package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.CreateIncidentNoteRequest;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuditEntryIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldCreateAuditEntryWhenIncidentIsCreated() throws Exception {
    CreateIncidentNoteRequest request =
        new CreateIncidentNoteRequest(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            null,
            IncidentSeverity.WARNING,
            "Latency increase observed",
            "Latency is elevated but below paging threshold.",
            "ops.test");

    mockMvc
        .perform(
            post("/api/v1/incidents")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/v1/audit-entries")
                .with(adminAuth())
                .param("action", "INCIDENT_NOTE_CREATED")
                .param("size", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data[0].action", is("INCIDENT_NOTE_CREATED")));
  }
}
