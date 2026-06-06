package com.fak.core;

import com.fak.audit.AuditService;
import com.fak.config.ConfigRegistry;
import com.fak.config.ConstraintRule;
import com.fak.metrics.MetricsService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
  private final ConfigRegistry registry;
  private final FactBuilder factBuilder;
  private final ConstraintEngine constraintEngine;
  private final ReplayHasher hasher;
  private final AuditService auditService;
  private final MetricsService metrics;

  public ValidationService(ConfigRegistry registry, FactBuilder factBuilder, ConstraintEngine constraintEngine,
      ReplayHasher hasher, AuditService auditService, MetricsService metrics) {
    this.registry = registry;
    this.factBuilder = factBuilder;
    this.constraintEngine = constraintEngine;
    this.hasher = hasher;
    this.auditService = auditService;
    this.metrics = metrics;
  }

  public ValidationDecision validate(ActionEnvelope envelope) {
    long start = System.nanoTime();
    String actionId = "act_" + UUID.randomUUID();
    String policyVersion = envelope != null && envelope.policyVersion() != null
        ? envelope.policyVersion()
        : registry.current().policyVersion();
    Map<String, Object> facts = factBuilder.build(envelope);
    List<ConstraintRule> matches = new ArrayList<>(constraintEngine.matching(registry.current().constraints(), facts));
    Decision decision = matches.stream().map(ConstraintRule::decision).reduce(Decision.ALLOW, Decision::max);
    String risk = matches.stream()
        .max(Comparator.comparing(rule -> rule.decision().precedence()))
        .map(ConstraintRule::risk)
        .orElse("low");
    String reason = matches.isEmpty()
        ? "No blocking constraints matched; action is allowed."
        : matches.stream().max(Comparator.comparing(rule -> rule.decision().precedence())).map(ConstraintRule::reason).orElse("");
    String hash = hasher.hash(envelope, decision, policyVersion);
    long latencyMs = Math.max(1, (System.nanoTime() - start) / 1_000_000);
    ValidationDecision result = new ValidationDecision(
        actionId,
        decision,
        risk,
        reason,
        matches.stream().map(ConstraintRule::id).toList(),
        policyVersion,
        hash,
        latencyMs,
        facts);
    if (auditService != null) {
      auditService.recordValidation(envelope, result);
    }
    metrics.record(result);
    return result;
  }
}
