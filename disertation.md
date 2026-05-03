# Formal Agent Kernel: A Framework-Agnostic Runtime Contract Validator for AI Agent Actions and Controlled Disclosure

## Abstract

This dissertation presents Formal Agent Kernel (FAK), a framework-agnostic runtime validation kernel for AI-agent actions. FAK separates agent reasoning from execution by requiring structured proposed actions to pass through a deterministic contract engine before reaching external systems. Proposed actions are normalized into structured envelopes and evaluated against machine-checkable contracts defined through YAML configuration. The system models each decision over agent identity, declared goal, action, context, and policy version, returning `ALLOW`, `DENY`, `REQUIRE_APPROVAL`, or `REQUIRE_CONFIRMATION`.

The implementation scope covers SQL and DevOps/shell action validation, audit logging, replay, and policy tests. A limited controlled-disclosure demo shows how the same validation model can extend to agent-to-agent context sharing by redacting or summarizing sensitive memory before disclosure. Evaluation compares FAK against no-guard, prompt-only, and schema-only baselines, measuring unsafe action blocking, false positives, validation latency, replay determinism, and cross-domain portability.

## 1. Introduction

Modern AI agent systems can reason, plan, use tools, coordinate tasks, and interact with external systems. Frameworks such as LangChain, LangGraph, AutoGen, CrewAI, OpenClaw, and MCP-style tool ecosystems make it increasingly easy to connect large language models to databases, files, APIs, deployment systems, cloud infrastructure, and persistent memory.

This creates a critical safety problem. AI models are probabilistic and can hallucinate, drift from instructions, misinterpret user intent, follow prompt injection, or propose unsafe tool calls. Prompt-only safeguards and output filters can reduce some risks, but they do not provide an independent runtime boundary between agent reasoning and real-world execution.

FAK addresses this gap by acting as a deterministic validation layer between an agent and anything it can affect. Agents may propose actions, but those actions must be structured, validated, and authorized before execution.

Core principle:

```text
Agents can reason freely, but cannot execute freely.
```

This dissertation focuses on the practical and defensible foundation of that idea: runtime validation of structured agent actions, evaluated across SQL and DevOps/shell domains, with auditability and replay. It also includes a small controlled-disclosure extension to show that the same model can govern agent-to-agent context sharing.

## 2. Problem Statement

Current AI-agent systems often rely on assumed correctness. Once an agent decides to call a tool, the surrounding framework may execute the action unless a developer has manually added checks. This is dangerous in environments involving:

- production databases
- deployment systems
- cloud infrastructure
- shell access
- file and repository mutations
- persistent memory
- multi-agent communication

The problem is not only whether an action is syntactically valid. A SQL query can be well-formed and still unsafe. A deployment command can be correctly structured and still outside change policy. A memory item can be relevant to another agent and still unsafe to disclose.

The research problem is:

```text
How can a framework-agnostic runtime validation kernel enforce deterministic, configurable contracts over structured AI-agent actions before execution?
```

## 3. Research Questions

RQ1: How can a framework-agnostic runtime validation kernel enforce deterministic, configurable contracts over structured AI-agent actions before execution?

RQ2: Can FAK reduce unsafe agent actions compared with no-guard, prompt-only, and schema-only baselines?

RQ3: What runtime overhead does FAK introduce?

RQ4: Can the same validation model be reused across action domains by changing configuration rather than core code?

RQ5: Can the same model support limited controlled agent-to-agent disclosure?

## 4. Proposed Approach

FAK is a declarative runtime contract validation system. It sits between agent reasoning and execution.

Runtime flow:

```text
Agent Reasoning
  -> Proposed Structured Action
  -> FAK Validation
  -> Decision
  -> Approved Execution / Denial / Approval / Confirmation
```

FAK uses a normalized action envelope so it can work across different agent frameworks. A framework adapter converts tool calls, memory requests, or delegation events into a standard structure. FAK then validates the envelope against configuration and action-pack logic.

Core execution tuple:

```text
⟨Agent, Goal, Action, Context, PolicyVersion⟩ ⊨ Constraints
```

Decision outputs:

```text
ALLOW
DENY
REQUIRE_APPROVAL
REQUIRE_CONFIRMATION
```

The controlled-disclosure extension uses a related tuple:

```text
⟨SenderAgent, ReceiverAgent, Goal, Resource, Operation, Context, PolicyVersion⟩ ⊨ TrustPolicy
```

Disclosure decisions include:

```text
SHARE_FULL
SHARE_REDACTED
SHARE_SUMMARY_ONLY
DENY_SHARE
```

## 5. Definition Of Formal

In this project, "formal" means structured, typed, machine-checkable contracts evaluated deterministically at runtime.

This dissertation does not claim full theorem-prover-backed formal verification. It does not claim mathematical proof of universal safety. Instead, it claims deterministic runtime contract enforcement, policy-versioned decisions, and replayable audit evidence.

## 6. System Architecture

The system architecture is:

```text
Agent Framework / Demo Agent
  -> Framework Adapter
  -> FAK Normalized Envelope
  -> Schema Validator
  -> Action / Trust Pack Analyzer
  -> Constraint Engine
  -> Decision Engine
  -> Approval Engine
  -> Audit Logger + Replay Engine
  -> Executor / Memory / Agent Router
```

This extends the original architecture of:

```text
AgentNode -> FAKNode -> ExecutionNode
```

with one key additional abstraction:

```text
Normalized Action Envelope
```

The normalized envelope is what makes FAK framework-agnostic.

## 7. Core Components

### Action Interceptor

Captures proposed agent operations before execution.

### Framework Adapter

Converts framework-specific tool calls or agent events into a normalized FAK envelope.

### Schema Validator

Ensures the envelope and action payload match the expected schema.

### Action Pack Analyzer

Extracts domain-specific facts. For example, the SQL pack extracts query operation, tables, columns, and destructive intent. The DevOps/shell pack classifies production changes, dangerous shell commands, and blast radius.

### Constraint Engine

Evaluates deterministic predicates over agent, goal, action, context, analyzer facts, and policy version.

### Decision Engine

Produces the final decision using precedence:

```text
DENY > REQUIRE_APPROVAL > REQUIRE_CONFIRMATION > ALLOW
```

### Approval Engine

Creates pending approval records for actions requiring human review.

### Audit Logger

Records validation, approval, denial, execution, replay, and disclosure events.

### Replay Engine

Re-evaluates past decisions using the same input and policy version.

## 8. Config-Driven Design

FAK behavior is defined through configuration rather than hardcoded policy logic.

Core configuration files:

- `agent_specs.yml`
- `action_schema.yml`
- `constraints.yml`
- `approval_policies.yml`
- `policy_tests.yml`

This supports portability across domains. The same FAK Core can evaluate SQL actions, DevOps/shell actions, and controlled-disclosure operations by loading different schemas, constraints, and packs.

## 9. Implementation Scope

In scope:

- framework-agnostic action envelope
- config-driven validation
- SQL action validation
- DevOps/shell action validation
- approval decisions
- audit logging
- replay
- policy test runner
- limited controlled-disclosure demo

Out of scope:

- full enterprise SaaS
- full OpenClaw integration
- hosted dashboard
- IAM/SSO/RBAC
- formal theorem proving
- distributed validation
- finance/healthcare packs
- full multi-agent negotiation protocol

## 10. Evaluation Plan

The evaluation compares:

- no guard
- prompt-only guard
- schema-only validation
- FAK validation

Metrics:

- unsafe action blocked rate
- false positive rate
- validation latency
- p95 validation latency
- replay determinism
- config portability
- disclosure precision in the limited demo

Scenarios:

- destructive SQL in production
- safe read-only SQL
- production deployment outside change window
- destructive shell command
- secret/file access attempt
- memory disclosure with sensitive fields
- delegation outside agent role

## 11. Controlled Disclosure Extension

The broader product vision treats FAK as a cognitive trust boundary for agent-to-agent communication. Agents with persistent memory should not share all relevant context by default. They should share only the minimum sufficient context required for another agent to complete an authorized task.

This dissertation includes only a limited demo of that idea.

Example:

```text
CareerAgent asks FinanceAgent for relocation context.
FAK shares budget range and risk preference.
FAK withholds visa document number, exact bank balance, and private emails.
```

Core principle:

```text
Least-Context Principle: an agent should disclose only the minimum sufficient context required for the receiver to complete an authorized task.
```

## 12. Expected Outcome

The project should demonstrate that:

- structured runtime validation can reduce unsafe agent execution
- policy behavior can be changed through configuration
- the same core validator can apply across multiple action domains
- validation overhead can remain acceptable for interactive agent workflows
- audit and replay can explain why an action was allowed, denied, escalated, or redacted
- the model can extend beyond tool execution into limited disclosure governance

## 13. Chapter Outline

1. Introduction
2. Literature Review
3. Formal Model
4. System Design
5. Implementation
6. Evaluation
7. Discussion
8. Conclusion

## 14. Conclusion

FAK provides a framework-agnostic runtime contract layer for AI agents. It separates reasoning from execution, normalizes proposed operations into structured envelopes, validates them against deterministic contracts, and records replayable audit evidence. The MSc implementation should prove the foundation through SQL and DevOps/shell action validation, while the controlled-disclosure demo shows the path toward a broader trust kernel for autonomous agent ecosystems.

