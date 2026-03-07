package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.CreateMonitoredServiceRequest;
import com.example.opsmonitor.api.dto.request.RetryFailedJobRequest;
import com.example.opsmonitor.domain.model.enums.RetryOutcome;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ApiAccessControlIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldAllowPublicHealthWithoutCredentials() throws Exception {
    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.applicationStatus").exists());
  }

  @Test
  void shouldAllowPublicReadinessWithoutCredentials() throws Exception {
    mockMvc
        .perform(get("/api/v1/health/readiness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("READY")));
  }

  @Test
  void shouldRejectSensitiveEndpointWithoutCredentials() throws Exception {
    mockMvc.perform(get("/api/v1/failed-jobs")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowViewerToReadButNotMutate() throws Exception {
    UUID failedJobId = UUID.fromString("44444444-1111-1111-1111-111111111111");
    RetryFailedJobRequest request =
        new RetryFailedJobRequest("ops.viewer", RetryOutcome.FAILED, "No permission");

    mockMvc.perform(get("/api/v1/failed-jobs").with(viewerAuth())).andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(viewerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowOperatorToRetryButDenyAuditReads() throws Exception {
    UUID failedJobId = UUID.fromString("44444444-1111-1111-1111-111111111111");
    RetryFailedJobRequest request =
        new RetryFailedJobRequest("ops.operator", RetryOutcome.FAILED, "Retry as operator");

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.failedJob.id", is(failedJobId.toString())));

    mockMvc
        .perform(get("/api/v1/audit-entries").with(operatorAuth()))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldDenyOperatorFromCreatingService() throws Exception {
    CreateMonitoredServiceRequest request =
        new CreateMonitoredServiceRequest(
            "restricted-create-service", "prod", "platform", "https://platform.internal/health");

    mockMvc
        .perform(
            post("/api/v1/services")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAdminToCreateServiceAndReadAuditEntries() throws Exception {
    CreateMonitoredServiceRequest request =
        new CreateMonitoredServiceRequest(
            "auth-check-service", "prod", "platform", "https://platform.internal/health");

    mockMvc
        .perform(
            post("/api/v1/services")
                .with(adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name", is("auth-check-service")));

    mockMvc.perform(get("/api/v1/audit-entries").with(adminAuth())).andExpect(status().isOk());
  }

  @Test
  void shouldKeepActuatorSeparatedFromApiRoles() throws Exception {
    mockMvc.perform(get("/actuator/health").with(adminAuth())).andExpect(status().isForbidden());
  }
}
