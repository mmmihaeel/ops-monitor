package com.example.opsmonitor.infrastructure.repository.specification;

import com.example.opsmonitor.domain.model.IncidentNote;
import com.example.opsmonitor.domain.model.enums.IncidentSeverity;
import com.example.opsmonitor.domain.model.enums.IncidentStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class IncidentNoteSpecifications {

  private IncidentNoteSpecifications() {}

  public static Specification<IncidentNote> serviceIdEquals(UUID serviceId) {
    return (root, ignored, builder) -> {
      if (serviceId == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("service").get("id"), serviceId);
    };
  }

  public static Specification<IncidentNote> failedJobIdEquals(UUID failedJobId) {
    return (root, ignored, builder) -> {
      if (failedJobId == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("failedJob").get("id"), failedJobId);
    };
  }

  public static Specification<IncidentNote> severityEquals(IncidentSeverity severity) {
    return (root, ignored, builder) -> {
      if (severity == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("severity"), severity);
    };
  }

  public static Specification<IncidentNote> statusEquals(IncidentStatus status) {
    return (root, ignored, builder) -> {
      if (status == null) {
        return builder.conjunction();
      }
      return builder.equal(root.get("status"), status);
    };
  }
}
