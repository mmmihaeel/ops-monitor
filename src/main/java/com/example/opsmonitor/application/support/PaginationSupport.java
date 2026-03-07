package com.example.opsmonitor.application.support;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationSupport {

  private static final Set<String> ALLOWED_DIRECTIONS = Set.of("asc", "desc");

  private PaginationSupport() {}

  public static Pageable pageable(
      int page, int size, String sortExpression, Set<String> allowedFields, String defaultField) {
    String sortField = defaultField;
    Sort.Direction direction = Sort.Direction.DESC;

    if (sortExpression != null && !sortExpression.isBlank()) {
      String[] parts = sortExpression.split(",");
      if (parts.length > 0) {
        String candidateField = parts[0].trim();
        if (allowedFields.contains(candidateField)) {
          sortField = candidateField;
        }
      }
      if (parts.length > 1) {
        String candidateDirection = parts[1].trim().toLowerCase();
        if (ALLOWED_DIRECTIONS.contains(candidateDirection)) {
          direction = Sort.Direction.fromString(candidateDirection);
        }
      }
    }

    return PageRequest.of(page, size, Sort.by(direction, sortField));
  }
}
