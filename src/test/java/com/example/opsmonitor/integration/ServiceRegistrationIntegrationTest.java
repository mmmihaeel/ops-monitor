package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.CreateMonitoredServiceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ServiceRegistrationIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldRegisterServiceAndListIt() throws Exception {
    CreateMonitoredServiceRequest request =
        new CreateMonitoredServiceRequest(
            "billing-api", "prod", "billing", "https://billing.internal/health");

    mockMvc
        .perform(
            post("/api/v1/services")
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name", is("billing-api")))
        .andExpect(jsonPath("$.data.environment", is("prod")))
        .andExpect(jsonPath("$.data.currentStatus", is("UNKNOWN")));

    mockMvc
        .perform(
            get("/api/v1/services").with(viewerAuth()).param("q", "billing").param("size", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].name", is("billing-api")));
  }

  @Test
  void shouldReturnConflictForDuplicateService() throws Exception {
    CreateMonitoredServiceRequest request =
        new CreateMonitoredServiceRequest(
            "payments-api", "prod", "payments", "https://payments.internal/health");

    mockMvc
        .perform(
            post("/api/v1/services")
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }
}
