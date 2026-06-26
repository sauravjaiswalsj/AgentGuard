package com.fak.core;

import com.fak.config.ConfigRegistry;
import com.fak.config.PolicyConfig;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * Orchestrates the full FAK validation pipeline:
 *   1. Pre-flight check (known agent, allowed goal, allowed action)
 *   2. Fact extraction via FactBuilder
 *   3. Constraint evaluation via ConstraintEngine
 *   4. Replay hash via ReplayHasher
 */
@Service
public class ValidationService {

    private final ConfigRegistry registry;
    private final FactBuilder     factBuilder;
    private final ConstraintEngine engine;

    public ValidationService(ConfigRegistry registry) {
        this.registry    = registry;
        this.factBuilder = new FactBuilder();
        this.engine      = new ConstraintEngine();
    }

    public ValidationDecision validate(ActionEnvelope envelope) {
        long start = System.currentTimeMillis();
        PolicyConfig policy = registry.getConfig();

        // Step 1 — structural pre-flight
        ValidationResult pre = preflight(envelope, policy);

        // Step 2 — build flat fact map
        Map<String, Object> facts = factBuilder.build(envelope, pre);

        // Step 3 — evaluate constraints
        ConstraintEngine.EvalResult result = engine.evaluate(policy.getConstraints(), facts);

        // Step 4 — hash envelope for replay
        String hash = ReplayHasher.hash(envelope);

        long latency = System.currentTimeMillis() - start;
        return new ValidationDecision(
            result.decision(), result.reason(), result.risk(),
            result.matchedConstraints(), hash, latency
        );
    }

    private ValidationResult preflight(ActionEnvelope env, PolicyConfig policy) {
        if (env == null || env.actor() == null || env.intent() == null || env.operation() == null) {
            return ValidationResult.invalid();
        }
        var spec = policy.getAgents().get(env.actor().agentId());
        boolean knownAgent   = spec != null;
        boolean goalAllowed  = knownAgent && spec.getAllowedGoals().contains(env.intent().goal());
        boolean actionAllowed= knownAgent && spec.getAllowedActions().contains(env.operation().type());
        return new ValidationResult(true, knownAgent, goalAllowed, actionAllowed);
    }
}
