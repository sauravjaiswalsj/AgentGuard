package com.fak.core;

import java.util.List;

/**
 * Full validation result returned to the calling agent.
 */
public record ValidationDecision(
    Decision decision,
    String reason,
    String risk,
    List<String> matchedConstraints,
    String replayHash,
    long latencyMs
) {
    public static ValidationDecision allow(String replayHash, long latencyMs) {
        return new ValidationDecision(Decision.ALLOW, "All constraints satisfied.", "none",
                List.of(), replayHash, latencyMs);
    }
}
