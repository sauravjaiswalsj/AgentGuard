# ContextGate: Governed Agent-to-Agent Working Memory

## Definition

ContextGate is a governed shared working memory for agent-to-agent communication, where FAK enforces least-context disclosure before one agent shares memory, state, or intent with another.

It is not a memory database. It is not vector search. It is not a shared pool where every agent sees everything.

It is closer to:

```text
Agent-to-agent working memory + selective disclosure + trust routing
```

## Preferred Terminology

- Product name: `ContextGate`
- Technical component: `Cognitive Context Kernel`
- Protocol idea: `Selective Context Protocol`
- Category: `A2A memory firewall`

Core positioning:

```text
ContextGate gives AI agents a need-to-know communication layer.
```

Long-term product thesis:

```text
ContextGate lets multi-agent systems collaborate without oversharing memory.
```

FAK remains the underlying research/kernel concept. ContextGate is the commercial wrapper around the agent-to-agent memory and disclosure product.

## Why This Exists

Human communication is selective. People do not transfer their full memory, identity, emotional state, documents, and private context to every person they collaborate with. They reveal the part relevant to the task, at the right level of detail, to the right person, sometimes with redaction, sometimes as a summary, and sometimes not at all.

AI agents currently lack this boundary. Persistent-memory agents can accumulate private user context, tool results, files, decisions, and plans. In a multi-agent system, one agent may ask another agent for context. Without a governance layer, the sender can over-share raw memory.

The central tension is:

```text
Coordination requires context.
Privacy requires restriction.
```

ContextGate resolves that tension:

```text
Share enough to collaborate.
Share no more than necessary.
```

## Core Principle

Least-context communication:

```text
An agent should receive the minimum sufficient context required to complete its assigned subtask.
```

This is analogous to least privilege, but applied to memory, context, and agent-to-agent communication.

## Architecture

```text
Agent A
  -> proposed message + memory context
  -> Cognitive Context Kernel
  -> need-to-know validation
  -> disclosure transformer
  -> redacted / summarized / approved context
  -> Agent B
```

FAK validates:

- who is sending
- who is receiving
- what the goal is
- what memory/context is requested
- what sensitivity level applies
- what the receiver actually needs
- what is allowed by policy
- what should be redacted
- what should be summarized
- what requires human approval

## General Contract

The original FAK action contract is:

```text
<Agent, Goal, Action, Context> |= Constraints
```

For agent-to-agent memory communication, this becomes:

```text
<SenderAgent, ReceiverAgent, Goal, Message, MemoryContext> |= DisclosurePolicy
```

The more general form is:

```text
<Actor, Recipient, Intent, Operation, Resource, Context> |= Policy
```

Where:

- `Operation` can be `communicate`, `delegate`, `disclose`, `request_memory`, or `share_summary`.
- `Resource` can be a memory item, context bundle, tool result, plan, or private state.
- `Policy` decides whether the resource can be shared, transformed, denied, or approval-gated.

## Core Components

### 1. Shared Working Memory

A short-lived task memory space. It acts like a meeting room for agents.

```json
{
  "workspace_id": "relocation_plan_001",
  "goal": "plan UK relocation",
  "participants": ["career_agent", "finance_agent", "housing_agent"],
  "allowed_context": ["salary_range", "city_preference", "timeline"],
  "blocked_context": ["passport_number", "exact_bank_balance"]
}
```

Agents do not receive the user's full memory. They receive only the context that belongs in this task workspace.

### 2. Memory Classifier

Each memory item is labeled before use in agent-to-agent communication.

```json
{
  "memory_id": "mem_123",
  "type": "financial",
  "sensitivity": "private",
  "share_mode": "summary_only",
  "owner": "user",
  "allowed_recipients": ["finance_agent"],
  "requires_approval": true
}
```

Labels support:

- sensitivity
- owner
- allowed recipients
- allowed operations
- summary/redaction mode
- approval requirement
- expiry

### 3. Need-To-Know Evaluator

The evaluator decides whether the receiver actually needs the requested information.

Examples:

```text
FinanceAgent needs budget range.
FinanceAgent does not need passport number.

HousingAgent needs city and rent range.
HousingAgent does not need visa history.

CodeReviewAgent needs the diff and test output.
CodeReviewAgent does not need the user's private career notes.
```

### 4. Disclosure Transformer

The transformer modifies memory before sharing.

Transformations:

```text
raw -> redacted
raw -> summary
raw -> category
raw -> range
raw -> deny
raw -> approval request
```

Examples:

```text
£12,430 exact savings -> "low five-figure savings"
passport number -> withheld
visa situation -> "work authorization constraint exists"
private recruiter messages -> withheld
```

### 5. Conversation Memory Ledger

Every inter-agent disclosure is logged.

```json
{
  "from": "career_agent",
  "to": "finance_agent",
  "shared": ["salary_range", "budget_range"],
  "withheld": ["passport_number", "exact_bank_balance"],
  "reason": "Least-context policy",
  "policy_version": "mem_pol_003",
  "timestamp": "2026-05-03T03:20:00Z"
}
```

The ledger supports audit, debugging, replay, and compliance.

### 6. Policy, Audit, And Replay Layer

FAK provides:

- deterministic disclosure decisions
- policy-versioned audit logs
- replay hashes
- approval records
- matched policy explanations

## Agent-To-Agent Envelope

Agents communicate through a standard envelope.

```json
{
  "message_id": "msg_001",
  "sender": {
    "agent_id": "career_agent",
    "role": "career_planner"
  },
  "receiver": {
    "agent_id": "finance_agent",
    "role": "financial_planner"
  },
  "intent": {
    "goal": "estimate_relocation_affordability",
    "purpose": "calculate budget range",
    "scope": "summary_only"
  },
  "requested_context": [
    "salary_expectation",
    "savings",
    "visa_status",
    "monthly_budget",
    "passport_details"
  ],
  "proposed_payload": {
    "salary_expectation": "£60k",
    "savings": "£12,430",
    "visa_status": "Graduate visa then sponsorship needed",
    "monthly_budget": "£1.5k-£2k",
    "passport_details": "..."
  }
}
```

FAK returns:

```json
{
  "decision": "SHARE_REDACTED",
  "approved_payload": {
    "salary_expectation": "£50k-£70k",
    "savings": "low five figures",
    "visa_status": "work authorization constraint may affect planning",
    "monthly_budget": "£1.5k-£2k"
  },
  "blocked_fields": ["passport_details"],
  "reason": "Finance agent needs affordability constraints, not identity documents.",
  "matched_policies": ["least_context", "identity_document_block"],
  "policy_version": "mem_pol_003",
  "replay_hash": "sha256:..."
}
```

## CareerAgent To FinanceAgent Example

Scenario:

```text
CareerAgent asks FinanceAgent to help plan whether the user can relocate to London.
```

CareerAgent has access to:

- exact bank balance
- visa status
- private job rejection history
- salary expectations
- monthly rent budget
- emotional frustration
- passport details
- target companies

Bad agent-to-agent communication:

```json
{
  "to": "finance_agent",
  "message": "Here is everything I know about the user...",
  "memory_dump": {
    "bank_balance": "£12,430",
    "visa_status": "Graduate visa then sponsorship needed",
    "passport_number": "...",
    "job_rejections": "...",
    "salary_expectation": "£60k",
    "rent_budget": "£1.5k-£2k"
  }
}
```

ContextGate-approved communication:

```json
{
  "to": "finance_agent",
  "message": "Please estimate relocation affordability.",
  "shared_context": {
    "target_city": "London",
    "target_salary_range": "£50k-£75k",
    "monthly_budget_range": "£1.5k-£2k",
    "relocation_timeline": "after MSc completion",
    "work_authorization_constraint": "may require employer sponsorship later"
  },
  "withheld": [
    "passport_number",
    "exact_bank_balance",
    "private_recruiter_messages",
    "raw_visa_documents"
  ]
}
```

## Brain Analogy

The "brain-like" framing maps to concrete system components:

| Brain-like function | ContextGate equivalent |
| --- | --- |
| Working memory | Temporary task context shared between agents |
| Long-term memory | Persistent user/agent memory store |
| Attention | Select only relevant memories |
| Inhibition | Block unsafe disclosure |
| Social judgement | Decide what another agent should know |
| Summarization | Share abstraction, not raw memory |
| Consent | Require approval for sensitive context |
| Episodic memory | Audit/replay of what was shared |
| Theory of mind | Model receiver role, goal, and need-to-know |

The most important functions are:

```text
attention + inhibition + selective disclosure
```

## Product Thesis

ContextGate is a memory firewall and context router for multi-agent systems.

It controls:

- what agents know
- what agents ask for
- what agents share
- what agents summarize
- what agents withhold

Strong product sentence:

```text
ContextGate gives AI agents a need-to-know communication layer.
```

## Dissertation Boundary

The MSc dissertation should not build the full Cognitive Context Kernel.

Dissertation scope:

```text
The same validation kernel used for tool execution can also validate agent-to-agent context disclosure.
```

Build one controlled-disclosure demo:

```text
Agent A requests memory/context from Agent B.
FAK returns ALLOW, DENY, SHARE_REDACTED, or SHARE_SUMMARY_ONLY.
```

That demo is enough to connect the larger product vision to the narrower, achievable dissertation.

## Final One-Liner

```text
ContextGate gives agents the equivalent of human social memory boundaries: they can collaborate, but they only reveal what the other agent needs to know.
```

