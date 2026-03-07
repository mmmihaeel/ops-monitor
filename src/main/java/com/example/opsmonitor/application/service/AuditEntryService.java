package com.example.opsmonitor.application.service;

import com.example.opsmonitor.api.dto.response.AuditEntryResponse;
import com.example.opsmonitor.application.support.DtoMapper;
import com.example.opsmonitor.domain.model.AuditEntry;
import com.example.opsmonitor.domain.model.enums.AuditAction;
import com.example.opsmonitor.infrastructure.repository.AuditEntryRepository;
import com.example.opsmonitor.infrastructure.repository.specification.AuditEntrySpecifications;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEntryService {

  private final AuditEntryRepository auditEntryRepository;
  private final ObjectMapper objectMapper;

  public AuditEntryService(AuditEntryRepository auditEntryRepository, ObjectMapper objectMapper) {
    this.auditEntryRepository = auditEntryRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void record(
      String entityType,
      String entityId,
      AuditAction action,
      String actor,
      Map<String, Object> details) {
    AuditEntry entry = new AuditEntry();
    entry.setEntityType(entityType);
    entry.setEntityId(entityId);
    entry.setAction(action);
    entry.setActor(actor);
    entry.setDetailsJson(serializeDetails(details));
    auditEntryRepository.save(entry);
  }

  @Transactional(readOnly = true)
  public Page<AuditEntryResponse> list(
      String entityType, AuditAction action, Instant from, Instant to, Pageable pageable) {
    Specification<AuditEntry> specification =
        Specification.where(AuditEntrySpecifications.entityTypeEquals(entityType))
            .and(AuditEntrySpecifications.actionEquals(action))
            .and(AuditEntrySpecifications.createdAtFrom(from))
            .and(AuditEntrySpecifications.createdAtTo(to));

    return auditEntryRepository.findAll(specification, pageable).map(DtoMapper::toResponse);
  }

  private String serializeDetails(Map<String, Object> details) {
    try {
      return objectMapper.writeValueAsString(details == null ? Map.of() : details);
    } catch (JsonProcessingException exception) {
      return "{}";
    }
  }
}
