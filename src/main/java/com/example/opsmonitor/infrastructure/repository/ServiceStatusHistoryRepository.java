package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.ServiceStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceStatusHistoryRepository extends JpaRepository<ServiceStatusHistory, Long> {

  List<ServiceStatusHistory> findTop10ByServiceIdOrderByChangedAtDesc(UUID serviceId);
}
