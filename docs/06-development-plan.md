# Development Plan

## Phase 1: Documentation Foundation

Deliverables:

- Create the markdown documentation pack.
- Align product fit, PRD, system design, dissertation scope, and evaluation plan.
- Keep `api-spec.yml` unchanged initially.
- Later update `api-spec.yml` to include richer decisions, replay, approval, and disclosure endpoints.

Acceptance criteria:

- Product vision, commercial wedge, and MSc scope are clearly separated.
- Dissertation scope is achievable and avoids overclaiming.
- Demo and evaluation plans are testable.

## Phase 2: FAK Core

Deliverables:

- Models
- Config loader
- Schema validator
- Constraint evaluator
- Decision engine
- Audit logger
- Replay hash

Acceptance criteria:

- A normalized action envelope can be validated.
- Invalid envelopes fail closed.
- A deterministic decision is returned.
- Audit events include policy version and replay hash.

## Phase 3: Action Validation Packs

Deliverables:

- SQL pack
- DevOps/shell pack
- Sample policies
- Policy tests

Acceptance criteria:

- SQL pack detects read/write/destructive operations.
- DevOps/shell pack detects production actions and destructive commands.
- Policies can be changed without editing core logic.
- Policy tests verify expected decisions.

## Phase 4: Adapters And API

Deliverables:

- Python SDK
- FastAPI service
- LangGraph adapter
- Generic webhook adapter

Acceptance criteria:

- Developers can call FAK in-process.
- External systems can call FAK over HTTP.
- LangGraph tool calls can be intercepted before execution.

## Phase 5: Controlled Disclosure Demo

Deliverables:

- Memory labels
- Disclosure envelope
- Redaction/summary decisions
- Audit logs for disclosure

Acceptance criteria:

- Agent-to-agent memory request can be validated.
- Sensitive fields can be withheld.
- Safe fields can be shared.
- The audit log records shared and blocked fields.

## Phase 6: Evaluation

Deliverables:

- Baselines
- Metrics
- Demo scenarios
- Dissertation results table

Acceptance criteria:

- FAK is compared against no guard, prompt-only guard, and schema-only validation.
- Unsafe action blocked rate and false positive rate are reported.
- Median and p95 validation latency are reported.
- Replay determinism is demonstrated.

