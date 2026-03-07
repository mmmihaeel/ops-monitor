package com.example.opsmonitor.infrastructure.repository;

import com.example.opsmonitor.domain.model.IncidentNote;
import com.example.opsmonitor.domain.model.enums.IncidentStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncidentNoteRepository
    extends JpaRepository<IncidentNote, UUID>, JpaSpecificationExecutor<IncidentNote> {

  long countByCreatedAtAfter(Instant timestamp);

  long countByStatusIn(Collection<IncidentStatus> statuses);
}
