package com.example.opsmonitor.api.support;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    String code, String message, List<FieldValidationError> errors, Instant timestamp) {

  public static ErrorResponse of(String code, String message, List<FieldValidationError> errors) {
    return new ErrorResponse(code, message, errors, Instant.now());
  }
}
