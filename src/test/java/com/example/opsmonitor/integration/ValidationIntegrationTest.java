package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.CreateIncidentNoteRequest;
import com.example.opsmonitor.api.dto.request.CreateMonitoredServiceRequest;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ValidationIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldFailValidationForInvalidServicePayload() throws Exception {
    CreateMonitoredServiceRequest request =
        new CreateMonitoredServiceRequest("", "prod", "", "not-a-url");

    mockMvc
        .perform(
            post("/api/v1/services")
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
  }

  @Test
  void shouldFailIncidentCreationWithoutReferences() throws Exception {
    CreateIncidentNoteRequest request =
        new CreateIncidentNoteRequest(
            null,
            null,
            IncidentSeverity.WARNING,
            "Missing relation",
            "Incident note must be linked",
            "ops.test");

    mockMvc
        .perform(
            post("/api/v1/incidents")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
  }

  @Test
  void shouldReturnBadRequestForMalformedJson() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/incidents")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"severity\":\"WARNING\","))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
  }
}
