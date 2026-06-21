package com.fak.core;

import com.fak.config.ConstraintRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Evaluates the ordered list of policy constraints against a flat fact map.
 * First matching DENY wins; first matching REQUIRE_APPROVAL is remembered but
 * evaluation continues (a later DENY can still override it).
 */
public class ConstraintEngine {
    private static final Logger log = LoggerFactory.getLogger(ConstraintEngine.class);

    public record EvalResult(
        Decision decision,
        String reason,
        String risk,
        List<String> matchedConstraints
    ) {}

    public EvalResult evaluate(List<ConstraintRule> rules, Map<String, Object> facts) {
        String pendingReason = null;
        String pendingRisk   = null;
        List<String> matched = new ArrayList<>();

        for (ConstraintRule rule : rules) {
            if (matches(rule.getWhen(), facts)) {
                matched.add(rule.getId());
                log.debug("Constraint matched: {} → {}", rule.getId(), rule.getDecision());
                Decision d = Decision.valueOf(rule.getDecision());
                if (d == Decision.DENY) {
                    return new EvalResult(Decision.DENY, rule.getReason(), rule.getRisk(), matched);
                }
                if (d == Decision.REQUIRE_APPROVAL && pendingReason == null) {
                    pendingReason = rule.getReason();
                    pendingRisk   = rule.getRisk();
                }
            }
        }

        if (pendingReason != null) {
            return new EvalResult(Decision.REQUIRE_APPROVAL, pendingReason, pendingRisk, matched);
        }
        return new EvalResult(Decision.ALLOW, "All constraints satisfied.", "none", matched);
    }

    private boolean matches(Map<String, Object> when, Map<String, Object> facts) {
        if (when == null) return false;
        for (Map.Entry<String, Object> cond : when.entrySet()) {
            Object actual = facts.get(cond.getKey());
            if (!Objects.equals(String.valueOf(actual), String.valueOf(cond.getValue()))) {
                return false;
            }
        }
        return true;
    }
}
