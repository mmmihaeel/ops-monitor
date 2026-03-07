package com.example.opsmonitor.infrastructure.repository.specification;

import com.example.opsmonitor.domain.model.FailedJob;
import com.example.opsmonitor.domain.model.enums.FailedJobState;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class FailedJobSpecifications {

  private FailedJobSpecifications() {}

  public static Specification<FailedJob> serviceIdEquals(UUID serviceId) {
    return (root, ignored, builder) -> {
      if (serviceId == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("service").get("id"), serviceId);
    };
  }

  public static Specification<FailedJob> stateEquals(FailedJobState state) {
    return (root, ignored, builder) -> {
      if (state == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("state"), state);
    };
  }

  public static Specification<FailedJob> jobTypeEquals(String jobType) {
    return (root, ignored, builder) -> {
      if (jobType == null || jobType.isBlank()) {
        return builder.conjunction();
      }
      return builder.equal(builder.lower(root.get("jobType")), jobType.trim().toLowerCase());
    };
  }
}
