# Runtime Contract Model

## Meaning Of Formal

In this project, "formal" means structured, typed, machine-checkable contracts evaluated deterministically at runtime.

This project does not claim:

- mathematical proof
- provably safe execution
- full formal verification
- theorem-prover-backed correctness

Instead, FAK claims:

- deterministic policy decisions
- machine-checkable runtime contracts
- typed action and disclosure envelopes
- replayable audit records
- policy-versioned validation

## Execution Contract

Core execution tuple:

```text
⟨Agent, Goal, Action, Context, PolicyVersion⟩ ⊨ Constraints
```

FAK computes:

```text
FAK(agent, goal, action, context, policy_version) -> decision
```

Where:

- `Agent` includes identity, role, framework, trust level, allowed goals, and allowed action space.
- `Goal` is the declared purpose or task the agent is pursuing.
- `Action` is a typed operation with target and parameters.
- `Context` includes environment, user role, approval state, sensitivity, change window, and runtime facts.
- `PolicyVersion` pins the exact policy/config used for evaluation.
- `Constraints` are deterministic predicates over the normalized envelope and analyzer facts.

## Trust And Disclosure Contract

Extended trust tuple:

```text
⟨SenderAgent, ReceiverAgent, Goal, Resource, Operation, Context, PolicyVersion⟩ ⊨ TrustPolicy
```

FAK computes:

```text
FAK(sender, receiver, goal, resource, operation, context, policy_version) -> trust_decision
```

This supports:

- memory access
- memory write
- agent-to-agent disclosure
- context redaction
- summary-only sharing
- delegation validation

## Decision Classes

Execution decisions:

```text
ALLOW
DENY
REQUIRE_APPROVAL
REQUIRE_CONFIRMATION
```

Disclosure decisions:

```text
SHARE_FULL
SHARE_REDACTED
SHARE_SUMMARY_ONLY
DENY_SHARE
```

## Decision Precedence

Execution precedence:

```text
DENY > REQUIRE_APPROVAL > REQUIRE_CONFIRMATION > ALLOW
```

Disclosure precedence:

```text
DENY_SHARE > SHARE_REDACTED > SHARE_SUMMARY_ONLY > SHARE_FULL
```

## Least-Context Principle

Least-Context Principle: an agent should disclose only the minimum sufficient context required for the receiver to complete an authorized task.

Formal intuition:

```text
Given task T and receiver R,
share minimal context C' subset C
such that R can complete T
and C' satisfies disclosure policy P.
```

The principle is analogous to least privilege, but for agent memory and context.

## Example Execution Contract

```yaml
constraint_id: prod_deploy_requires_approval
when:
  operation.type: kubernetes.apply
  context.environment: production
  context.approval_state: none
decision: REQUIRE_APPROVAL
reason: Production deployments require approval.
```

## Example Disclosure Contract

```yaml
constraint_id: identity_document_never_shared_without_user_approval
when:
  operation.type: memory.disclose
  resource.sensitivity: identity_document
  context.user_consent: none
decision: DENY_SHARE
reason: Identity documents require explicit user approval before disclosure.
```

