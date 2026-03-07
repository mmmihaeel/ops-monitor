package com.example.opsmonitor.infrastructure.cache;

import com.example.opsmonitor.api.dto.response.HealthSummaryResponse;
import com.example.opsmonitor.api.dto.response.ServiceStatusResponse;
import com.example.opsmonitor.infrastructure.config.CacheSettingsProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatusCacheService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatusCacheService.class);
  private static final String GLOBAL_SUMMARY_KEY = "ops-monitor:status-summary";
  private static final String SERVICE_STATUS_KEY_PREFIX = "ops-monitor:service-status:";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final CacheSettingsProperties cacheSettingsProperties;

  public StatusCacheService(
      StringRedisTemplate redisTemplate,
      ObjectMapper objectMapper,
      CacheSettingsProperties cacheSettingsProperties) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.cacheSettingsProperties = cacheSettingsProperties;
  }

  public Optional<HealthSummaryResponse> getGlobalSummary() {
    return read(GLOBAL_SUMMARY_KEY, HealthSummaryResponse.class)
        .map(
            response ->
                new HealthSummaryResponse(
                    response.applicationStatus(),
                    response.totalServices(),
                    response.servicesUp(),
                    response.servicesDegraded(),
                    response.servicesDown(),
                    response.servicesUnknown(),
                    response.activeFailedJobs(),
                    response.activeIncidents(),
                    response.incidentNotesLast24h(),
                    response.generatedAt(),
                    true));
  }

  public void putGlobalSummary(HealthSummaryResponse response) {
    write(GLOBAL_SUMMARY_KEY, response, cacheSettingsProperties.getGlobalSummaryTtl());
  }

  public void evictGlobalSummary() {
    redisTemplate.delete(GLOBAL_SUMMARY_KEY);
  }

  public Optional<ServiceStatusResponse> getServiceStatus(UUID serviceId) {
    String key = serviceKey(serviceId);
    return read(key, ServiceStatusResponse.class)
        .map(
            response ->
                new ServiceStatusResponse(
                    response.serviceId(),
                    response.serviceName(),
                    response.environment(),
                    response.currentStatus(),
                    response.lastSnapshotAt(),
                    response.history(),
                    response.recentSnapshots(),
                    true));
  }

  public void putServiceStatus(UUID serviceId, ServiceStatusResponse response) {
    write(serviceKey(serviceId), response, cacheSettingsProperties.getServiceStatusTtl());
  }

  public void evictServiceStatus(UUID serviceId) {
    redisTemplate.delete(serviceKey(serviceId));
  }

  private String serviceKey(UUID serviceId) {
    return SERVICE_STATUS_KEY_PREFIX + serviceId;
  }

  private <T> Optional<T> read(String key, Class<T> type) {
    try {
      String payload = redisTemplate.opsForValue().get(key);
      if (payload == null || payload.isBlank()) {
        return Optional.empty();
      }
      return Optional.ofNullable(objectMapper.readValue(payload, type));
    } catch (Exception exception) {
      LOGGER.warn("Could not read cache key {}", key, exception);
      return Optional.empty();
    }
  }

  private void write(String key, Object value, java.time.Duration ttl) {
    try {
      String payload = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, payload, ttl);
    } catch (JsonProcessingException exception) {
      LOGGER.warn("Could not serialize cache value for key {}", key, exception);
    }
  }
}
