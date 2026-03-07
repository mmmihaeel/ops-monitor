package com.example.opsmonitor.application.service;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class RetryBackoffPolicy {

  public Duration nextDelay(int attemptNumber) {
    if (attemptNumber <= 1) {
      return Duration.ofMinutes(1);
    }
    if (attemptNumber == 2) {
      return Duration.ofMinutes(5);
    }
    return Duration.ofMinutes(15);
  }
}
