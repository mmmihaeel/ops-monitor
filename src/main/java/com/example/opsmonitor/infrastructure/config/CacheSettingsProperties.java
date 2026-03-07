package com.example.opsmonitor.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ops.monitor.cache")
public class CacheSettingsProperties {

  private Duration globalSummaryTtl = Duration.ofSeconds(30);
  private Duration serviceStatusTtl = Duration.ofSeconds(20);

  public Duration getGlobalSummaryTtl() {
    return globalSummaryTtl;
  }

  public void setGlobalSummaryTtl(Duration globalSummaryTtl) {
    this.globalSummaryTtl = globalSummaryTtl;
  }

  public Duration getServiceStatusTtl() {
    return serviceStatusTtl;
  }

  public void setServiceStatusTtl(Duration serviceStatusTtl) {
    this.serviceStatusTtl = serviceStatusTtl;
  }
}
