package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.MonitoredService;
import com.example.opsmonitor.domain.model.enums.ServiceStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MonitoredServiceRepository
    extends JpaRepository<MonitoredService, UUID>, JpaSpecificationExecutor<MonitoredService> {

  boolean existsByNameIgnoreCaseAndEnvironmentIgnoreCase(String name, String environment);

  long countByCurrentStatus(ServiceStatus currentStatus);
}
