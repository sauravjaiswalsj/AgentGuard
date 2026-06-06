package com.fak.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.Instant;

@Entity
public class AuditEvent {
  @Id
  private String id;
  private Instant createdAt;
  private String eventType;
  private String decision;
  private String policyVersion;
  private String replayHash;
  @Lob
  private String envelopeJson;
  @Lob
  private String decisionJson;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public String getEventType() { return eventType; }
  public void setEventType(String eventType) { this.eventType = eventType; }
  public String getDecision() { return decision; }
  public void setDecision(String decision) { this.decision = decision; }
  public String getPolicyVersion() { return policyVersion; }
  public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
  public String getReplayHash() { return replayHash; }
  public void setReplayHash(String replayHash) { this.replayHash = replayHash; }
  public String getEnvelopeJson() { return envelopeJson; }
  public void setEnvelopeJson(String envelopeJson) { this.envelopeJson = envelopeJson; }
  public String getDecisionJson() { return decisionJson; }
  public void setDecisionJson(String decisionJson) { this.decisionJson = decisionJson; }
}
