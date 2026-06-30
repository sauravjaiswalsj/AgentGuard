package com.fak.core;

import com.fak.config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTest {

    private ValidationService svc;

    @BeforeEach
    void setUp() {
        // Build a minimal policy in-memory — no Spring context needed
        PolicyConfig policy = new PolicyConfig();

        AgentSpec reporterSpec = new AgentSpec();
        reporterSpec.setRole("database_reporter");
        reporterSpec.setAllowedGoals(List.of("generate_report"));
        reporterSpec.setAllowedActions(List.of("sql.query"));
        policy.setAgents(Map.of("reporting_agent", reporterSpec));

        ConstraintRule deny = new ConstraintRule();
        deny.setId("destructive_sql_denied");
        deny.setWhen(Map.of("analysis.sql.destructive", "true"));
        deny.setDecision("DENY");
        deny.setRisk("critical");
        deny.setReason("Destructive SQL is blocked before execution.");
        policy.setConstraints(List.of(deny));

        ConfigRegistry registry = new ConfigRegistry(policy);
        svc = new ValidationService(registry);
    }

    @Test
    void safe_select_is_allowed() {
        var env = envelope("SELECT id FROM customers");
        var result = svc.validate(env);
        assertThat(result.decision()).isEqualTo(Decision.ALLOW);
    }

    @Test
    void delete_statement_is_denied() {
        var env = envelope("DELETE FROM customers WHERE last_login < '2022-01-01'");
        var result = svc.validate(env);
        assertThat(result.decision()).isEqualTo(Decision.DENY);
        assertThat(result.matchedConstraints()).contains("destructive_sql_denied");
    }

    private ActionEnvelope envelope(String query) {
        return new ActionEnvelope(
            new Actor("reporting_agent", "database_reporter", "langgraph", "internal"),
            new Intent("generate_report", "monthly report"),
            new Operation("sql.query", "customers", Map.of("query", query)),
            Map.of("environment", "production", "userRole", "analyst", "approvalState", "none")
        );
    }
}
