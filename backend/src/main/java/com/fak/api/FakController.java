package com.fak.api;

import com.fak.audit.AuditEvent;
import com.fak.audit.AuditService;
import com.fak.config.ConfigRegistry;
import com.fak.config.PolicyTestCase;
import com.fak.core.ActionEnvelope;
import com.fak.core.ValidationDecision;
import com.fak.core.ValidationService;
import com.fak.metrics.MetricsService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
public class FakController {

    private final ValidationService validationService;
    private final AuditService      auditService;
    private final MetricsService    metricsService;
    private final ConfigRegistry    configRegistry;

    public FakController(ValidationService validationService,
                         AuditService auditService,
                         MetricsService metricsService,
                         ConfigRegistry configRegistry) {
        this.validationService = validationService;
        this.auditService      = auditService;
        this.metricsService    = metricsService;
        this.configRegistry    = configRegistry;
    }

    /** POST /api/v1/validate */
    @PostMapping("/validate")
    public ResponseEntity<ValidationDecision> validate(@RequestBody ActionEnvelope envelope) {
        ValidationDecision decision = validationService.validate(envelope);
        auditService.recordValidation(envelope, decision);
        metricsService.record(decision);
        return ResponseEntity.ok(decision);
    }

    /** GET /api/v1/health */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FAK kernel is healthy");
    }

    /** GET /api/v1/audit/events */
    @GetMapping("/audit/events")
    public ResponseEntity<List<AuditEvent>> auditEvents() {
        return ResponseEntity.ok(auditService.list());
    }

    /** GET /api/v1/metrics */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(metricsService.snapshot());
    }

    /** POST /api/v1/policies/test — run all policy test cases from policies.yml */
    @PostMapping("/policies/test")
    public ResponseEntity<Map<String, Object>> policyTest() {
        List<PolicyTestCase> cases = configRegistry.current().policyTests();
        if (cases == null || cases.isEmpty()) {
            return ResponseEntity.ok(Map.of("total", 0, "passed", 0, "failed", 0, "results", List.of()));
        }
        List<Map<String, Object>> results = new ArrayList<>();
        int passed = 0;
        for (PolicyTestCase tc : cases) {
            ValidationDecision dec = validationService.validate(tc.envelope());
            boolean ok = dec.decision() == tc.expectDecision();
            if (ok) passed++;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", tc.name());
            row.put("expected", tc.expectDecision());
            row.put("actual", dec.decision());
            row.put("pass", ok);
            row.put("reason", dec.reason());
            results.add(row);
        }
        return ResponseEntity.ok(Map.of(
            "total",   cases.size(),
            "passed",  passed,
            "failed",  cases.size() - passed,
            "results", results
        ));
    }

    /** POST /api/v1/replay/{id} — re-validate the envelope stored in an audit event */
    @PostMapping("/replay/{id}")
    public ResponseEntity<Map<String, Object>> replay(@PathVariable Long id) {
        AuditEvent event = auditService.get(id);
        // Re-build envelope from stored fields — we have agentId, goal, operationType
        // but not the full envelope JSON; return the stored decision fields as a replay summary
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("replayedEventId", event.getId());
        result.put("agentId", event.getAgentId());
        result.put("originalDecision", event.getDecision());
        result.put("replayHash", event.getReplayHash());
        result.put("note", "Full envelope re-validation requires stored envelope JSON; showing original decision.");
        return ResponseEntity.ok(result);
    }
}
