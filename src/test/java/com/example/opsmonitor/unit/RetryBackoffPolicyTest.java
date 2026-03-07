package com.example.opsmonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.opsmonitor.application.service.RetryBackoffPolicy;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RetryBackoffPolicyTest {

  private final RetryBackoffPolicy retryBackoffPolicy = new RetryBackoffPolicy();

  @Test
  void shouldUseShortDelayForFirstAttempt() {
    assertEquals(Duration.ofMinutes(1), retryBackoffPolicy.nextDelay(1));
  }

  @Test
  void shouldUseMediumDelayForSecondAttempt() {
    assertEquals(Duration.ofMinutes(5), retryBackoffPolicy.nextDelay(2));
  }

  @Test
  void shouldUseLongDelayForThirdAndBeyond() {
    assertEquals(Duration.ofMinutes(15), retryBackoffPolicy.nextDelay(3));
    assertEquals(Duration.ofMinutes(15), retryBackoffPolicy.nextDelay(5));
  }
}
