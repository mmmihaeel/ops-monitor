package com.example.opsmonitor.domain.model.enums;

public enum FailedJobState {
  FAILED,
  RETRY_IN_PROGRESS,
  RETRY_SCHEDULED,
  RECOVERED,
  EXHAUSTED
}
