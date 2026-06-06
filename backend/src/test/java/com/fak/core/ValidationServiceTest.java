package com.fak.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.fak.config.ConfigRegistry;
import com.fak.metrics.MetricsService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ValidationServiceTest {
  ValidationService validationService;

  @BeforeEach
  void setUp() throws Exception {
    ConfigRegistry registry = new ConfigRegistry(new ClassPathResource("configs/policies.yml"));
    validationService = new ValidationService(
        registry,
        new FactBuilder(registry),
        new ConstraintEngine(),
        new ReplayHasher(),
        null,
        new MetricsService());
  }

  // ── Existing tests ────────────────────────────────────────────────────────

  @Test
  void safeSelectIsAllowed() {
    ValidationDecision result = validationService.validate(sql("SELECT id, name FROM customers"));
    assertThat(result.decision()).isEqualTo(Decision.ALLOW);
  }

  @Test
  void productionDeleteByReportingAgentIsDenied() {
    ValidationDecision result = validationService.validate(sql("DELETE FROM customers WHERE last_login < '2022-01-01'"));
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("reporting_goal_read_only", "destructive_sql_denied");
  }

  @Test
  void productionDeployRequiresApproval() {
    ValidationDecision result = validationService.validate(deploy("production", false));
    assertThat(result.decision()).isEqualTo(Decision.REQUIRE_APPROVAL);
  }

  @Test
  void destructiveShellIsDenied() {
    ValidationDecision result = validationService.validate(shell("rm -rf /var/app/data"));
    assertThat(result.decision()).isEqualTo(Decision.DENY);
  }

  @Test
  void unknownOperationFailsClosed() {
    ActionEnvelope envelope = new ActionEnvelope(
        new Actor("reporting_agent", "test", "database_reporter", "internal"),
        new Intent("generate_report", "monthly report"),
        new Operation("email.send", "external", Map.of("body", "leak")),
        Map.of("environment", "production", "approvalState", "none"),
        null);
    ValidationDecision result = validationService.validate(envelope);
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("disallowed_action_denied");
  }

  // ── New tests ─────────────────────────────────────────────────────────────

  @Test
  void nullEnvelopeFailsClosed() {
    ValidationDecision result = validationService.validate(null);
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("invalid_envelope_denied");
  }

  @Test
  void envelopeWithMissingActorFailsClosed() {
    ActionEnvelope envelope = new ActionEnvelope(
        null,
        new Intent("generate_report", "monthly report"),
        new Operation("sql.query", "customers", Map.of("query", "SELECT 1")),
        Map.of(),
        null);
    ValidationDecision result = validationService.validate(envelope);
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("invalid_envelope_denied");
  }

  @Test
  void unknownAgentIsDenied() {
    ActionEnvelope envelope = new ActionEnvelope(
        new Actor("rogue_agent", "test", "hacker", "external"),
        new Intent("steal_data", "exfiltrate"),
        new Operation("sql.query", "customers", Map.of("query", "SELECT * FROM customers")),
        Map.of("environment", "production"),
        null);
    ValidationDecision result = validationService.validate(envelope);
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("unknown_agent_denied");
  }

  @Test
  void piiExportToExternalIsDenied() {
    ActionEnvelope envelope = new ActionEnvelope(
        new Actor("reporting_agent", "test", "database_reporter", "internal"),
        new Intent("generate_report", "export customer emails"),
        new Operation("sql.query", "customers", Map.of("query", "SELECT email, phone FROM customers")),
        Map.of("environment", "production", "approvalState", "none", "destination", "external"),
        null);
    ValidationDecision result = validationService.validate(envelope);
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("pii_export_denied");
  }

  @Test
  void safeStagingDeployIsAllowed() {
    ValidationDecision result = validationService.validate(deploy("staging", true));
    assertThat(result.decision()).isEqualTo(Decision.ALLOW);
  }

  @Test
  void productionSqlWriteRequiresApproval() {
    ActionEnvelope envelope = new ActionEnvelope(
        new Actor("deploy_agent", "test", "deployment_operator", "internal"),
        new Intent("deploy_production_release", "update records"),
        new Operation("sql.query", "orders", Map.of("query", "UPDATE orders SET status='shipped' WHERE id=1")),
        Map.of("environment", "production", "approvalState", "none"),
        null);
    ValidationDecision result = validationService.validate(envelope);
    assertThat(result.decision()).isEqualTo(Decision.REQUIRE_APPROVAL);
    assertThat(result.matchedConstraints()).contains("production_sql_write_requires_approval");
  }

  @Test
  void deployOutsideChangeWindowRequiresApproval() {
    ValidationDecision result = validationService.validate(deploy("production", false));
    assertThat(result.decision()).isEqualTo(Decision.REQUIRE_APPROVAL);
    assertThat(result.matchedConstraints()).contains("outside_change_window_requires_approval");
  }

  @Test
  void kubectlDeleteIsDenied() {
    ValidationDecision result = validationService.validate(shell("kubectl delete namespace production"));
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("destructive_shell_denied");
  }

  @Test
  void terraformDestroyIsDenied() {
    ValidationDecision result = validationService.validate(shell("terraform destroy -auto-approve"));
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("destructive_shell_denied");
  }

  @Test
  void replayHashIsDeterministic() {
    ActionEnvelope envelope = sql("SELECT id FROM customers");
    ValidationDecision first  = validationService.validate(envelope);
    ValidationDecision second = validationService.validate(envelope);
    assertThat(first.replayHash()).isEqualTo(second.replayHash());
  }

  @Test
  void disallowedGoalIsDenied() {
    ActionEnvelope envelope = new ActionEnvelope(
        new Actor("reporting_agent", "test", "database_reporter", "internal"),
        new Intent("deploy_production_release", "sneaky deploy"),
        new Operation("sql.query", "customers", Map.of("query", "SELECT 1")),
        Map.of("environment", "production", "approvalState", "none"),
        null);
    ValidationDecision result = validationService.validate(envelope);
    assertThat(result.decision()).isEqualTo(Decision.DENY);
    assertThat(result.matchedConstraints()).contains("disallowed_goal_denied");
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private ActionEnvelope sql(String query) {
    return new ActionEnvelope(
        new Actor("reporting_agent", "test", "database_reporter", "internal"),
        new Intent("generate_report", "monthly report"),
        new Operation("sql.query", "customers", Map.of("query", query)),
        Map.of("environment", "production", "approvalState", "none"),
        null);
  }

  private ActionEnvelope deploy(String environment, boolean changeWindow) {
    String goal = "production".equals(environment) ? "deploy_production_release" : "deploy_staging_release";
    return new ActionEnvelope(
        new Actor("deploy_agent", "test", "deployment_operator", "internal"),
        new Intent(goal, "deploy"),
        new Operation("devops.deploy", "payments-service", Map.of("version", "1.2.0")),
        Map.of("environment", environment, "approvalState", "none", "changeWindow", changeWindow),
        null);
  }

  private ActionEnvelope shell(String command) {
    return new ActionEnvelope(
        new Actor("deploy_agent", "test", "deployment_operator", "internal"),
        new Intent("deploy_staging_release", "run command"),
        new Operation("shell.command", "server", Map.of("command", command)),
        Map.of("environment", "production", "approvalState", "none"),
        null);
  }
}
