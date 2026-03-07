package com.example.opsmonitor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired private org.flywaydb.core.Flyway flyway;

  @BeforeEach
  void resetState() {
    flyway.clean();
    flyway.migrate();

    var connectionFactory = redisTemplate.getConnectionFactory();
    if (connectionFactory != null) {
      var connection = connectionFactory.getConnection();
      connection.serverCommands().flushDb();
      connection.close();
    }
  }

  protected RequestPostProcessor viewerAuth() {
    return SecurityMockMvcRequestPostProcessors.httpBasic("test_viewer", "test_viewer_password");
  }

  protected RequestPostProcessor operatorAuth() {
    return SecurityMockMvcRequestPostProcessors.httpBasic(
        "test_operator", "test_operator_password");
  }

  protected RequestPostProcessor adminAuth() {
    return SecurityMockMvcRequestPostProcessors.httpBasic("test_admin", "test_admin_password");
  }

  protected RequestPostProcessor managementAuth() {
    return SecurityMockMvcRequestPostProcessors.httpBasic(
        "test_management", "test_management_password");
  }
}
