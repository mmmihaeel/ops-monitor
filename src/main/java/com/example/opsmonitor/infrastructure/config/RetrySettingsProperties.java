package com.example.opsmonitor.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ops.monitor.retry")
public class RetrySettingsProperties {

  private Duration lockTtl = Duration.ofSeconds(30);

  public Duration getLockTtl() {
    return lockTtl;
  }

  public void setLockTtl(Duration lockTtl) {
    this.lockTtl = lockTtl;
  }
}
