package com.example.opsmonitor.infrastructure.repository.specification;

import com.example.opsmonitor.domain.model.AuditEntry;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public final class AuditEntrySpecifications {

  private AuditEntrySpecifications() {}

  public static Specification<AuditEntry> entityTypeEquals(String entityType) {
    return (root, ignored, builder) -> {
      if (entityType == null || entityType.isBlank()) {
        return builder.conjunction();
      }
      return builder.equal(builder.lower(root.get("entityType")), entityType.trim().toLowerCase());
    };
  }

  public static Specification<AuditEntry> actionEquals(AuditAction action) {
    return (root, ignored, builder) -> {
      if (action == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("action"), action);
    };
  }

  public static Specification<AuditEntry> createdAtFrom(Instant from) {
    return (root, ignored, builder) -> {
      if (from == null) {
        return builder.conjunction();
      }
      return builder.greaterThanOrEqualTo(root.get("createdAt"), from);
    };
  }

  public static Specification<AuditEntry> createdAtTo(Instant to) {
    return (root, ignored, builder) -> {
      if (to == null) {
        return builder.conjunction();
      }
      return builder.lessThanOrEqualTo(root.get("createdAt"), to);
    };
  }
}
