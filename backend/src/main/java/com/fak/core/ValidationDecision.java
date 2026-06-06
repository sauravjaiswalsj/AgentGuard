package com.fak.core;

import java.util.List;
import java.util.Map;

public record ValidationDecision(
    String actionId,
    Decision decision,
    String risk,
    String reason,
    List<String> matchedConstraints,
    String policyVersion,
    String replayHash,
    long latencyMs,
    Map<String, Object> facts
) {
  public boolean allowed() {
    return decision == Decision.ALLOW;
  }
}
