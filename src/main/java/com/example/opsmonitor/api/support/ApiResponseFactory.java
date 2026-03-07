package com.example.opsmonitor.api.support;

import org.springframework.data.domain.Page;

public final class ApiResponseFactory {

  private ApiResponseFactory() {}

  public static <T> ApiResponse<T> ok(T body) {
    return ApiResponse.of(body);
  }

  public static <T> ApiResponse<Iterable<T>> page(Page<T> page) {
    PageMeta pageMeta =
        new PageMeta(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious());
    return ApiResponse.of(page.getContent(), pageMeta);
  }
}
