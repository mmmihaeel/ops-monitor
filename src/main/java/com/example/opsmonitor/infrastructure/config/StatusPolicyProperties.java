package com.example.opsmonitor.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ops.monitor.status-policy")
public class StatusPolicyProperties {

  private int degradedLatencyThresholdMs = 1200;
  private int downLatencyThresholdMs = 5000;

  public int getDegradedLatencyThresholdMs() {
    return degradedLatencyThresholdMs;
  }

  public void setDegradedLatencyThresholdMs(int degradedLatencyThresholdMs) {
    this.degradedLatencyThresholdMs = degradedLatencyThresholdMs;
  }

  public int getDownLatencyThresholdMs() {
    return downLatencyThresholdMs;
  }

  public void setDownLatencyThresholdMs(int downLatencyThresholdMs) {
    this.downLatencyThresholdMs = downLatencyThresholdMs;
  }
}
