package com.example.opsmonitor.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class ManagementAuthIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldRequireAuthForActuator() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowActuatorWithConfiguredCredentials() throws Exception {
    mockMvc
        .perform(get("/actuator/health").with(managementAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("UP")));
  }
}
