package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.FailedJob;
import com.example.opsmonitor.domain.model.enums.FailedJobState;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FailedJobRepository
    extends JpaRepository<FailedJob, UUID>, JpaSpecificationExecutor<FailedJob> {

  long countByStateIn(Collection<FailedJobState> states);
}
