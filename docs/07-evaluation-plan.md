# Evaluation Plan

## Goal

Evaluate whether FAK can enforce deterministic, configurable runtime contracts over structured AI-agent actions and limited disclosures.

## Baselines

Compare:

- no guard
- prompt-only guard
- schema-only validation
- FAK validation

## Metrics

Primary metrics:

- unsafe action blocked rate
- false positive rate
- validation latency
- p95 latency
- replay determinism
- policy portability
- disclosure precision in the controlled demo

## Scenarios

Execution scenarios:

- destructive SQL in production
- safe read-only SQL
- production deployment outside change window
- destructive shell command
- secret/file access attempt
- safe staging deployment
- safe non-mutating shell command

Disclosure scenarios:

- memory disclosure with sensitive fields
- summary-only memory sharing
- delegation outside agent role
- allowed delegation within role and goal

## Experimental Setup

For each scenario:

1. Create a structured action or disclosure envelope.
2. Run it through each baseline.
3. Record whether unsafe behavior reaches execution.
4. Record the decision, reason, matched constraints, latency, and replay hash.
5. Replay FAK decisions under the same policy version.

## Expected Results Format

Example table:

| Scenario | No Guard | Prompt Only | Schema Only | FAK | Expected FAK Decision |
| --- | --- | --- | --- | --- | --- |
| Production DELETE | Executes | May execute | Valid schema | Blocked | DENY |
| Prod deploy outside window | Executes | May execute | Valid schema | Approval | REQUIRE_APPROVAL |
| Safe SELECT | Executes | Executes | Valid schema | Executes | ALLOW |
| Private memory disclosure | Shares | May share | Valid schema | Redacted | SHARE_REDACTED |

## Dissertation Claims Supported

This evaluation supports these claims:

- FAK reduces unsafe action execution compared with weaker baselines.
- FAK adds measurable but bounded validation overhead.
- FAK decisions are replayable under pinned policy versions.
- FAK Core can be reused across domains through action packs and configuration.
- The same envelope model can support a limited disclosure-control extension.

