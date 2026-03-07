package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opsmonitor.api.dto.request.RetryFailedJobRequest;
import com.example.opsmonitor.domain.model.enums.RetryOutcome;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class FailedJobRetryIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldRetryFailedJobAndMarkRecoveredOnSuccess() throws Exception {
    UUID failedJobId = UUID.fromString("44444444-1111-1111-1111-111111111111");
    RetryFailedJobRequest request =
        new RetryFailedJobRequest(
            "ops.test", RetryOutcome.SUCCESS, "Recovered after dependency restart");

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.failedJob.id", is(failedJobId.toString())))
        .andExpect(jsonPath("$.data.failedJob.state", is("RECOVERED")))
        .andExpect(jsonPath("$.data.retryAttempt.attemptNumber", greaterThanOrEqualTo(1)));

    mockMvc
        .perform(get("/api/v1/failed-jobs/{id}", failedJobId).with(viewerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.failedJob.state", is("RECOVERED")));

    mockMvc
        .perform(
            get("/api/v1/audit-entries")
                .with(adminAuth())
                .param("action", "FAILED_JOB_RETRY_REQUESTED")
                .param("size", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].action", is("FAILED_JOB_RETRY_REQUESTED")));
  }

  @Test
  void shouldRejectRetryWhenJobIsExhausted() throws Exception {
    UUID failedJobId = UUID.fromString("44444444-3333-3333-3333-333333333333");
    RetryFailedJobRequest request =
        new RetryFailedJobRequest("ops.test", RetryOutcome.FAILED, "No-op");

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }

  @Test
  void shouldReturnNotFoundForRetryUnknownFailedJob() throws Exception {
    UUID failedJobId = UUID.fromString("99999999-9999-9999-9999-999999999999");
    RetryFailedJobRequest request =
        new RetryFailedJobRequest("ops.test", RetryOutcome.FAILED, "No-op");

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("NOT_FOUND")));
  }

  @Test
  void shouldRejectRetryWhenJobAlreadyRecovered() throws Exception {
    UUID failedJobId = UUID.fromString("44444444-1111-1111-1111-111111111111");
    RetryFailedJobRequest firstAttempt =
        new RetryFailedJobRequest("ops.test", RetryOutcome.SUCCESS, "Recover the job");

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstAttempt)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.failedJob.state", is("RECOVERED")));

    RetryFailedJobRequest secondAttempt =
        new RetryFailedJobRequest("ops.test", RetryOutcome.FAILED, "Retry after recovery");

    mockMvc
        .perform(
            post("/api/v1/failed-jobs/{id}/retry", failedJobId)
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondAttempt)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }
}
