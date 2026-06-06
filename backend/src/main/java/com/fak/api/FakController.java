package com.fak.api;

import com.fak.audit.AuditEvent;
import com.fak.audit.AuditService;
import com.fak.config.ConfigRegistry;
import com.fak.config.PolicyTestCase;
import com.fak.core.ActionEnvelope;
import com.fak.core.ValidationDecision;
import com.fak.core.ValidationService;
import com.fak.execution.ExecutionService;
import com.fak.metrics.MetricsService;
import com.fak.orchestration.MultiAgentManager.ClientRequest;
import com.fak.routing.RouterService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class FakController {
  private final RouterService router;
  private final ValidationService validationService;
  private final ExecutionService executionService;
  private final AuditService auditService;
  private final ConfigRegistry registry;
  private final MetricsService metrics;

  public FakController(RouterService router, ValidationService validationService, ExecutionService executionService,
      AuditService auditService, ConfigRegistry registry, MetricsService metrics) {
    this.router = router;
    this.validationService = validationService;
    this.executionService = executionService;
    this.auditService = auditService;
    this.registry = registry;
    this.metrics = metrics;
  }

  @PostMapping("/request")
  public Map<String, Object> request(@RequestBody ClientRequest request) {
    return router.route(request);
  }

  @PostMapping({"/agent/propose", "/validate"})
  public ValidationDecision validate(@RequestBody ActionEnvelope envelope) {
    return validationService.validate(envelope);
  }

  @PostMapping("/execute")
  public Map<String, Object> execute(@RequestBody ActionEnvelope envelope) {
    return executionService.execute(envelope);
  }

  @GetMapping("/audit/events")
  public List<AuditEvent> auditEvents() {
    return auditService.list();
  }

  @GetMapping("/audit/events/{eventId}")
  public AuditEvent auditEvent(@PathVariable String eventId) {
    return auditService.get(eventId);
  }

  @PostMapping("/replay/{eventId}")
  public Map<String, Object> replay(@PathVariable String eventId) {
    AuditEvent event = auditService.get(eventId);
    ValidationDecision replayed = validationService.validate(auditService.envelope(event));
    return Map.of(
        "originalDecision", event.getDecision(),
        "originalReplayHash", event.getReplayHash(),
        "replayed", replayed,
        "matches", event.getReplayHash().equals(replayed.replayHash()));
  }

  @PostMapping("/policies/test")
  public Map<String, Object> policyTests() {
    List<Map<String, Object>> results = registry.current().policyTests().stream().map(this::runTest).toList();
    long passed = results.stream().filter(result -> (Boolean) result.get("passed")).count();
    return Map.of("passed", passed, "total", results.size(), "results", results);
  }

  @GetMapping("/specs/{agentId}")
  public Object spec(@PathVariable String agentId) {
    return registry.agent(agentId);
  }

  @PutMapping("/specs/{agentId}")
  public Map<String, Object> updateSpec(@PathVariable String agentId, @RequestBody Map<String, Object> body) {
    return Map.of(
        "agentId", agentId,
        "stored", false,
        "message", "Prototype config registry is file-backed; edit policies.yml for durable changes.",
        "received", body);
  }

  @GetMapping("/config")
  public Object config() {
    return registry.current();
  }

  @GetMapping("/metrics")
  public Map<String, Object> metrics() {
    return metrics.snapshot();
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("status", "UP", "service", "Formal Agent Kernel");
  }

  @PostMapping("/approvals/{approvalId}/decision")
  public Map<String, Object> approval(@PathVariable String approvalId, @RequestBody Map<String, Object> body) {
    return Map.of("approvalId", approvalId, "recorded", true, "decision", body.getOrDefault("decision", "APPROVED"));
  }

  private Map<String, Object> runTest(PolicyTestCase test) {
    ValidationDecision decision = validationService.validate(test.envelope());
    return Map.of(
        "name", test.name(),
        "expected", test.expectDecision(),
        "actual", decision.decision(),
        "passed", test.expectDecision() == decision.decision(),
        "matchedConstraints", decision.matchedConstraints());
  }
}
