package com.fak.execution;

import com.fak.core.ActionEnvelope;
import com.fak.core.Decision;
import com.fak.core.ValidationDecision;
import com.fak.core.ValidationService;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ExecutionService {
  private final ValidationService validationService;

  public ExecutionService(ValidationService validationService) {
    this.validationService = validationService;
  }

  public Map<String, Object> execute(ActionEnvelope envelope) {
    ValidationDecision decision = validationService.validate(envelope);
    if (decision.decision() != Decision.ALLOW) {
      return Map.of("executed", false, "decision", decision, "message", "Execution blocked by FAK decision.");
    }
    return Map.of(
        "executed", true,
        "decision", decision,
        "mode", "SIMULATED",
        "executedAt", Instant.now().toString(),
        "message", "Action was allowed and simulated execution completed.");
  }
}
