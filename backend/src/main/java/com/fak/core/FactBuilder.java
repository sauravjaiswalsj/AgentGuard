package com.fak.core;

import com.fak.config.AgentSpec;
import com.fak.config.ConfigRegistry;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FactBuilder {
  private final ConfigRegistry registry;

  public FactBuilder(ConfigRegistry registry) {
    this.registry = registry;
  }

  public Map<String, Object> build(ActionEnvelope envelope) {
    Map<String, Object> facts = new LinkedHashMap<>();
    boolean valid = envelope != null
        && envelope.actor() != null
        && envelope.intent() != null
        && envelope.operation() != null
        && text(envelope.actor().agentId())
        && text(envelope.intent().goal())
        && text(envelope.operation().type());
    put(facts, "validation.valid", valid);

    if (!valid) {
      put(facts, "validation.knownAgent", false);
      return facts;
    }

    AgentSpec spec = registry.agent(envelope.actor().agentId());
    put(facts, "validation.knownAgent", spec != null);
    put(facts, "actor.agentId", envelope.actor().agentId());
    put(facts, "actor.role", envelope.actor().role());
    put(facts, "intent.goal", envelope.intent().goal());
    put(facts, "operation.type", envelope.operation().type());
    put(facts, "operation.target", envelope.operation().target());
    (envelope.context() == null ? Map.<String, Object>of() : envelope.context())
        .forEach((key, value) -> put(facts, "context." + key, value));

    if (spec != null) {
      put(facts, "validation.goalAllowed", spec.allowedGoals().contains(envelope.intent().goal()));
      put(facts, "validation.actionAllowed", spec.allowedActions().contains(envelope.operation().type()));
    }

    analyzeSql(envelope, facts);
    analyzeShell(envelope, facts);
    return facts;
  }

  private void analyzeSql(ActionEnvelope envelope, Map<String, Object> facts) {
    if (!"sql.query".equals(envelope.operation().type())) {
      return;
    }
    String query = String.valueOf(envelope.operation().parameters().getOrDefault("query", ""));
    String normalized = query.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    String operation = normalized.isBlank() ? "UNKNOWN" : normalized.split(" ")[0];
    boolean write = operation.matches("INSERT|UPDATE|DELETE|MERGE|CREATE|ALTER|DROP|TRUNCATE");
    boolean destructive = operation.matches("DROP|TRUNCATE|DELETE");
    boolean containsPii = normalized.matches(".*\\b(EMAIL|PHONE|NATIONAL_INSURANCE_NUMBER|SSN)\\b.*");
    put(facts, "analysis.sql.operation", operation);
    put(facts, "analysis.sql.write", write);
    put(facts, "analysis.sql.destructive", destructive);
    put(facts, "analysis.sql.containsPii", containsPii);
  }

  private void analyzeShell(ActionEnvelope envelope, Map<String, Object> facts) {
    if (!"shell.command".equals(envelope.operation().type()) && !"devops.deploy".equals(envelope.operation().type())) {
      return;
    }
    String command = String.valueOf(envelope.operation().parameters().getOrDefault("command", ""));
    String lower = command.toLowerCase(Locale.ROOT);
    boolean destructive = lower.contains("rm -rf")
        || lower.contains("kubectl delete")
        || lower.contains("terraform destroy")
        || lower.contains("drop database");
    put(facts, "analysis.shell.destructive", destructive);
  }

  private static boolean text(String value) {
    return value != null && !value.isBlank();
  }

  public static void put(Map<String, Object> root, String path, Object value) {
    String[] parts = path.split("\\.");
    Map<String, Object> cursor = root;
    for (int i = 0; i < parts.length - 1; i++) {
      Object next = cursor.computeIfAbsent(parts[i], ignored -> new LinkedHashMap<String, Object>());
      if (next instanceof Map<?, ?> nextMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = (Map<String, Object>) nextMap;
        cursor = typed;
      }
    }
    cursor.put(parts[parts.length - 1], value);
  }
}
