package com.example.opsmonitor.integration;

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

class StatusHistoryIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldRecordStatusTransitionInHistory() throws Exception {
    UUID serviceId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId, ServiceStatus.UP, 70, null, SnapshotSource.MANUAL, null);

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
        .andExpect(jsonPath("$.data.currentStatus", is("UP")))
        .andExpect(jsonPath("$.data.history[0].newStatus", is("UP")));
  }

  @Test
  void shouldNotAppendHistoryWhenStatusDoesNotChange() throws Exception {
    UUID serviceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    int historyCountBefore =
        objectMapper
            .readTree(
                mockMvc
                    .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .path("data")
            .path("history")
            .size();

    CreateHealthSnapshotRequest request =
        new CreateHealthSnapshotRequest(
            serviceId, ServiceStatus.UP, 90, null, SnapshotSource.API, null);

    mockMvc
        .perform(
            post("/api/v1/health-snapshots")
                .with(operatorAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("UP")));

    int historyCountAfter =
        objectMapper
            .readTree(
                mockMvc
                    .perform(get("/api/v1/services/{id}/status", serviceId).with(viewerAuth()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .path("data")
            .path("history")
            .size();

    org.junit.jupiter.api.Assertions.assertEquals(historyCountBefore, historyCountAfter);
  }
}
