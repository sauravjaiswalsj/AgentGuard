package com.fak.audit;

import com.fak.core.Decision;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

/**
 * Persisted record of every FAK validation decision.
 * Enables post-hoc audit, replay, and analytics.
 */
@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String      agentId;
    private String      goal;
    private String      operationType;

    @Enumerated(EnumType.STRING)
    private Decision    decision;

    private String      reason;
    private String      risk;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> matchedConstraints;

    private String      replayHash;
    private long        latencyMs;
    private Instant     createdAt = Instant.now();

    // Constructors, getters, setters
    public AuditEvent() {}

    public AuditEvent(String agentId, String goal, String operationType,
                      Decision decision, String reason, String risk,
                      List<String> matchedConstraints, String replayHash, long latencyMs) {
        this.agentId            = agentId;
        this.goal               = goal;
        this.operationType      = operationType;
        this.decision           = decision;
        this.reason             = reason;
        this.risk               = risk;
        this.matchedConstraints = matchedConstraints;
        this.replayHash         = replayHash;
        this.latencyMs          = latencyMs;
    }

    public Long getId()                   { return id; }
    public String getAgentId()            { return agentId; }
    public Decision getDecision()         { return decision; }
    public String getReplayHash()         { return replayHash; }
    public long getLatencyMs()            { return latencyMs; }
    public Instant getCreatedAt()         { return createdAt; }
}
