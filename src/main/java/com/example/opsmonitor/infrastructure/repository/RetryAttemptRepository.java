package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.RetryAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryAttemptRepository extends JpaRepository<RetryAttempt, UUID> {

  List<RetryAttempt> findByFailedJobIdOrderByTriggeredAtDesc(UUID failedJobId);
}
