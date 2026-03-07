package com.example.opsmonitor.api.support;

import java.time.Instant;

public record ApiResponse<T>(T data, ResponseMeta meta) {

  public static <T> ApiResponse<T> of(T data) {
    return new ApiResponse<>(data, new ResponseMeta(Instant.now(), null));
  }

  public static <T> ApiResponse<T> of(T data, PageMeta page) {
    return new ApiResponse<>(data, new ResponseMeta(Instant.now(), page));
  }
}
