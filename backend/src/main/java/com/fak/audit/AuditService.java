package com.fak.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fak.core.ActionEnvelope;
import com.fak.core.ValidationDecision;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final AuditRepository repository;
  private final ObjectMapper mapper;

  public AuditService(AuditRepository repository, ObjectMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  public AuditEvent recordValidation(ActionEnvelope envelope, ValidationDecision decision) {
    AuditEvent event = new AuditEvent();
    event.setId(UUID.randomUUID().toString());
    event.setCreatedAt(Instant.now());
    event.setEventType("VALIDATION");
    event.setDecision(decision.decision().name());
    event.setPolicyVersion(decision.policyVersion());
    event.setReplayHash(decision.replayHash());
    event.setEnvelopeJson(write(envelope));
    event.setDecisionJson(write(decision));
    return repository.save(event);
  }

  public List<AuditEvent> list() {
    return repository.findAll().stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .toList();
  }

  public AuditEvent get(String id) {
    return repository.findById(id).orElseThrow();
  }

  public ActionEnvelope envelope(AuditEvent event) {
    try {
      return mapper.readValue(event.getEnvelopeJson(), ActionEnvelope.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Unable to parse stored envelope", ex);
    }
  }

  private String write(Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Unable to serialize audit payload", ex);
    }
  }
}
