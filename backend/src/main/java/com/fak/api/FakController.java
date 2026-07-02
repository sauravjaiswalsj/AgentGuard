package com.fak.api;

import com.fak.audit.AuditService;
import com.fak.core.ActionEnvelope;
import com.fak.core.ValidationDecision;
import com.fak.core.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Primary REST endpoint for FAK validation.
 *
 * POST /api/v1/validate  →  ValidationDecision (JSON)
 */
@RestController
@RequestMapping("/api/v1")
public class FakController {

    private final ValidationService validationService;
    private final AuditService      auditService;

    public FakController(ValidationService validationService, AuditService auditService) {
        this.validationService = validationService;
        this.auditService      = auditService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationDecision> validate(@RequestBody ActionEnvelope envelope) {
        ValidationDecision decision = validationService.validate(envelope);
        auditService.record(envelope, decision);
        return ResponseEntity.ok(decision);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FAK kernel is healthy");
    }
}
