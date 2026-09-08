package com.fak.audit;

import com.fak.core.ActionEnvelope;
import com.fak.core.ValidationDecision;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    public AuditEvent recordValidation(ActionEnvelope envelope, ValidationDecision decision) {
        AuditEvent event = new AuditEvent(
            envelope.actor().agentId(),
            envelope.intent().goal(),
            envelope.operation().type(),
            decision.decision(),
            decision.reason(),
            decision.risk(),
            decision.matchedConstraints(),
            decision.replayHash(),
            decision.latencyMs()
        );
        return repository.save(event);
    }

    public List<AuditEvent> list() {
        return repository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
    }

    public AuditEvent get(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
