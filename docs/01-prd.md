# Product Requirements Document

## Product Name

Formal Agent Kernel (FAK)

## Product Summary

FAK is a framework-agnostic runtime trust kernel for AI agents. It validates agent actions, memory access, delegation, and disclosure before anything is executed or shared.

## Product Layers

### Layer 1: Execution Governance

Execution governance validates production-impacting actions before they run.

Requirements:

- Validate SQL queries, shell commands, file edits, deployments, cloud actions, and API mutations.
- Return `ALLOW`, `DENY`, `REQUIRE_APPROVAL`, or `REQUIRE_CONFIRMATION`.
- Provide audit logs, replay, policy tests, and approval workflows.
- Fail closed when an action is malformed, unknown, or missing required policy context.

### Layer 2: Agent Trust And Disclosure

Agent trust and disclosure governs what agents can access, request, reveal, and delegate.

Requirements:

- Validate memory access.
- Filter agent-to-agent disclosure.
- Enforce least-context sharing.
- Validate delegation between agents.
- Redact or summarize sensitive context before sharing.
- Require approval for sensitive memory disclosure.

## User Stories

- As a developer, I can wrap any agent framework with FAK using a normalized envelope.
- As a platform engineer, I can block unsafe production actions.
- As a security engineer, I can define constraints for actions, memory, and disclosure through YAML.
- As a compliance user, I can replay why an action or disclosure was allowed or denied.
- As an agent-system builder, I can limit what one agent shares with another.
- As a reviewer, I can approve, reject, or require confirmation for risky actions.

## V1 Product Scope

V1 includes:

- FAK Core
- Python SDK
- FastAPI service
- LangGraph adapter
- SQL action pack
- DevOps/shell action pack
- Controlled disclosure demo
- YAML policy/config system
- Audit/replay
- Policy test runner

## V1 Non-Goals

V1 does not include:

- Full SaaS dashboard
- SSO/RBAC
- Finance/healthcare packs
- Full OpenClaw protocol implementation
- Formal theorem proving
- Distributed validators
- Production multi-tenant hosting
- Kubernetes admission controller mode

## Core Inputs

FAK validates a normalized envelope:

```json
{
  "actor": {
    "agent_id": "deploy_agent",
    "framework": "langgraph",
    "role": "deployment_operator",
    "trust_level": "internal"
  },
  "intent": {
    "goal": "deploy_staging_release",
    "declared_purpose": "ship tested build"
  },
  "operation": {
    "type": "kubernetes.apply",
    "target": "payments-service",
    "parameters": {
      "environment": "production",
      "manifest": "deployment.yaml"
    }
  },
  "context": {
    "user_role": "developer",
    "change_window": false,
    "approval_state": "none"
  },
  "policy_version": "pol_2026_05_03_001"
}
```

## Core Outputs

```json
{
  "decision": "REQUIRE_APPROVAL",
  "risk": "high",
  "reason": "Production deployment outside change window requires platform-owner approval.",
  "matched_constraints": [
    "prod_deploy_requires_approval",
    "change_window_required"
  ],
  "required_approver": "platform_owner",
  "policy_version": "pol_2026_05_03_001",
  "action_id": "act_123",
  "replay_hash": "sha256:..."
}
```

## Functional Requirements

- Load agent specs, action schemas, constraints, approval policies, and policy tests from config.
- Validate every action envelope before execution.
- Run action/trust pack analyzers to extract normalized facts.
- Evaluate deterministic constraints against normalized facts.
- Apply decision precedence: `DENY > REQUIRE_APPROVAL > REQUIRE_CONFIRMATION > ALLOW`.
- Log every validation, approval, execution, denial, config change, and disclosure event.
- Replay historical events under pinned policy versions.
- Run policy tests from fixtures.

## Success Criteria

- Unsafe production actions are blocked or escalated in demo scenarios.
- Safe actions are allowed without unnecessary approval.
- The same core validator handles SQL and DevOps/shell actions using different configs/packs.
- Controlled-disclosure demo shows redacted or summary-only sharing.
- Replay produces identical decisions for the same input and policy version.
- Median validation latency remains acceptable for interactive agent use.

