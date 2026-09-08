package com.fak.core;

import com.fak.config.ConfigRegistry;
import com.fak.config.PolicyConfig;
import org.springframework.stereotype.Service;
import java.util.Map;

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
        PolicyConfig policy = registry.current();

        ValidationResult pre = preflight(envelope, policy);
        Map<String, Object> facts = factBuilder.build(envelope, pre);
        ConstraintEngine.EvalResult result = engine.evaluate(policy.constraints(), facts);
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
        var spec = policy.agents().get(env.actor().agentId());
        boolean knownAgent    = spec != null;
        boolean goalAllowed   = knownAgent && spec.allowedGoals().contains(env.intent().goal());
        boolean actionAllowed = knownAgent && spec.allowedActions().contains(env.operation().type());
        return new ValidationResult(true, knownAgent, goalAllowed, actionAllowed);
    }
}
