package com.example.opsmonitor.infrastructure.repository.specification;

import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import org.springframework.data.jpa.domain.Specification;

public final class MonitoredServiceSpecifications {

  private MonitoredServiceSpecifications() {}

  public static Specification<MonitoredService> nameContains(String query) {
    return (root, ignored, builder) -> {
      if (query == null || query.isBlank()) {
        return builder.conjunction();
      }
      String likeExpression = "%" + query.trim().toLowerCase() + "%";
      return builder.like(builder.lower(root.get("name")), likeExpression);
    };
  }

  public static Specification<MonitoredService> environmentEquals(String environment) {
    return (root, ignored, builder) -> {
      if (environment == null || environment.isBlank()) {
        return builder.conjunction();
      }
      return builder.equal(
          builder.lower(root.get("environment")), environment.trim().toLowerCase());
    };
  }

  public static Specification<MonitoredService> statusEquals(ServiceStatus status) {
    return (root, ignored, builder) -> {
      if (status == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("currentStatus"), status);
    };
  }
}
