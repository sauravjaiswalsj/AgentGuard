package com.fak.orchestration;

import com.fak.core.ActionEnvelope;
import com.fak.core.Actor;
import com.fak.core.Intent;
import com.fak.core.Operation;
import com.fak.core.ValidationDecision;
import com.fak.core.ValidationService;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MultiAgentManager {
  private final ValidationService validationService;

  public MultiAgentManager(ValidationService validationService) {
    this.validationService = validationService;
  }

  public Map<String, Object> handle(ClientRequest request) {
    ActionEnvelope envelope = request.query().toLowerCase().contains("deploy")
        ? deployEnvelope(request)
        : sqlEnvelope(request);
    ValidationDecision decision = validationService.validate(envelope);
    return Map.of("status", decision.decision().name(), "proposedAction", envelope, "decision", decision);
  }

  private ActionEnvelope sqlEnvelope(ClientRequest request) {
    String query = request.query().toLowerCase().contains("delete")
        ? "DELETE FROM customers WHERE last_login < '2022-01-01'"
        : "SELECT id, name FROM customers";
    return new ActionEnvelope(
        new Actor("reporting_agent", "java-simulator", "database_reporter", "internal"),
        new Intent("generate_report", request.query()),
        new Operation("sql.query", "customers", Map.of("query", query)),
        Map.of("environment", "production", "userRole", "analyst", "approvalState", "none"),
        null);
  }

  private ActionEnvelope deployEnvelope(ClientRequest request) {
    return new ActionEnvelope(
        new Actor("deploy_agent", "java-simulator", "deployment_operator", "internal"),
        new Intent("deploy_production_release", request.query()),
        new Operation("devops.deploy", "payments-service", Map.of("version", "1.2.0")),
        Map.of("environment", "production", "userRole", "developer", "approvalState", "none", "changeWindow", false),
        null);
  }

  public record ClientRequest(String userId, String query, Map<String, Object> context) {}
}
