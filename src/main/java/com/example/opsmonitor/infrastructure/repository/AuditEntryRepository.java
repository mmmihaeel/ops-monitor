package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditEntryRepository
    extends JpaRepository<AuditEntry, Long>, JpaSpecificationExecutor<AuditEntry> {}
