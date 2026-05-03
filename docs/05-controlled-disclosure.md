# Controlled Disclosure

## Concept

FAK acts as a cognitive trust boundary for agent-to-agent communication.

It governs:

- memory read
- memory write
- memory disclosure
- context redaction
- summary-only sharing
- delegation requests
- user approval for sensitive sharing

This is the long-term moat beyond production-action authorization.

## Why It Matters

Agents with persistent memory and tool access can accumulate sensitive context. In a multi-agent system, the problem is no longer only:

```text
Can this agent execute this action?
```

It becomes:

```text
What should this agent reveal to another agent?
```

Relevant context is not always safe context. A memory search result may help another agent complete a task, but it may also contain private identity data, sensitive notes, secrets, or information outside the receiver's purpose.

## Least-Context Disclosure

FAK should enforce:

```text
Share the minimum sufficient context required for the receiving agent to complete the authorized task.
```

Disclosure can be:

- full
- redacted
- summarized
- denied
- approval-gated

## Example Workflow

```text
CareerAgent asks FinanceAgent for relocation context.
FAK shares budget range and risk preference.
FAK withholds visa document number, exact bank balance, and private emails.
```

Decision:

```json
{
  "decision": "SHARE_REDACTED",
  "shared": {
    "monthly_budget_range": "1500-2000 GBP",
    "risk_preference": "low",
    "relocation_goal": "UK software engineering role"
  },
  "withheld": [
    "visa_document_number",
    "exact_bank_balance",
    "private_employer_emails"
  ],
  "reason": "FinanceAgent needs planning context, not raw identity documents or private messages.",
  "matched_constraints": [
    "least_context_disclosure",
    "identity_data_requires_user_approval"
  ],
  "policy_version": "mem_pol_004",
  "replay_hash": "sha256:..."
}
```

## Memory Labels

Memory items should carry labels:

```json
{
  "memory_id": "mem_982",
  "labels": {
    "sensitivity": "private",
    "scope": "personal",
    "allowed_agents": ["career_agent"],
    "share_mode": "summary_only",
    "requires_approval": true,
    "expires_at": null
  }
}
```

## MSc Scope

Controlled disclosure should be a limited demo in the MSc project, not the main build.

In scope:

- simple memory labels
- disclosure envelope
- redaction or summary-only decision
- audit event for what was shared and withheld

Out of scope:

- full OpenClaw protocol implementation
- vector memory governance
- long-term memory synchronization
- multi-agent negotiation protocol
- automated semantic minimization of context

