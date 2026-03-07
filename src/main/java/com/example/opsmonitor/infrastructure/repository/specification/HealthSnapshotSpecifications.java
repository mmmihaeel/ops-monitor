package com.example.opsmonitor.infrastructure.repository.specification;

import com.example.opsmonitor.domain.model.HealthSnapshot;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class HealthSnapshotSpecifications {

  private HealthSnapshotSpecifications() {}

  public static Specification<HealthSnapshot> serviceIdEquals(UUID serviceId) {
    return (root, ignored, builder) -> {
      if (serviceId == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("service").get("id"), serviceId);
    };
  }

  public static Specification<HealthSnapshot> statusEquals(ServiceStatus status) {
    return (root, ignored, builder) -> {
      if (status == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("status"), status);
    };
  }

  public static Specification<HealthSnapshot> recordedAtFrom(Instant from) {
    return (root, ignored, builder) -> {
      if (from == null) {
        return builder.conjunction();
      }
      return builder.greaterThanOrEqualTo(root.get("recordedAt"), from);
    };
  }

  public static Specification<HealthSnapshot> recordedAtTo(Instant to) {
    return (root, ignored, builder) -> {
      if (to == null) {
        return builder.conjunction();
      }
      return builder.lessThanOrEqualTo(root.get("recordedAt"), to);
    };
  }
}
