package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.HealthSnapshot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HealthSnapshotRepository
    extends JpaRepository<HealthSnapshot, UUID>, JpaSpecificationExecutor<HealthSnapshot> {

  List<HealthSnapshot> findTop5ByServiceIdOrderByRecordedAtDesc(UUID serviceId);
}
