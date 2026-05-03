# ContextGate

## Need-To-Know Memory For AI Agents

```text
AI agents can collaborate, but they should not overshare context.
```

## Slide 1: One-Liner

ContextGate is need-to-know memory for AI agents.

It lets agents collaborate without oversharing sensitive user or enterprise context.

## Slide 2: Problem

AI agents are moving from solo assistants to teams.

Soon, agents will hand tasks to other agents:

- career agents talking to finance agents
- coding agents talking to deployment agents
- support agents talking to billing agents
- security agents talking to infrastructure agents

But persistent-memory agents have no social boundary.

They can overshare private user context, internal documents, credentials, emotional notes, or business data.

Humans disclose selectively. Agents currently do not.

```text
Coordination requires context.
Privacy requires restriction.
```

## Slide 3: Why Now

Agents are gaining:

- persistent memory
- tool access
- planning
- multi-agent workflows
- MCP-style integrations
- long-running workspace context

The next trust problem is not only what agents can execute.

It is what agents can reveal to each other.

Enterprises will not allow autonomous agents to freely share internal memory across roles, systems, and workflows.

## Slide 4: Solution

ContextGate sits between agents.

It validates every agent-to-agent memory or context request and returns:

- full share
- redacted share
- summary-only share
- denial
- approval required

Core idea:

```text
Share enough to collaborate.
Share no more than necessary.
```

## Slide 5: Demo

CareerAgent asks FinanceAgent to help estimate whether the user can relocate to London.

Raw memory contains:

- passport details
- exact bank balance
- visa documents
- private recruiter messages
- salary goals
- job rejection history
- rent budget

FinanceAgent receives:

- target city
- target salary range
- monthly budget range
- relocation timeline
- high-level work authorization constraint

ContextGate withholds:

- passport number
- exact bank balance
- raw visa documents
- private recruiter messages
- emotionally sensitive notes

Decision:

```json
{
  "decision": "SHARE_REDACTED",
  "reason": "FinanceAgent needs affordability constraints, not identity documents or private messages."
}
```

## Slide 6: Product

ContextGate includes:

- agent-to-agent envelope
- memory labels
- need-to-know evaluator
- disclosure transformer
- audit/replay ledger
- framework adapters

Architecture:

```text
Agent A
  -> proposed message + memory context
  -> Cognitive Context Kernel
  -> need-to-know validation
  -> disclosure transformer
  -> redacted / summarized / approved context
  -> Agent B
```

The underlying research kernel is FAK. The commercial product is ContextGate.

## Slide 7: Market

Initial users:

- AI platform teams
- agent framework builders
- enterprise agent teams
- security/compliance teams

First wedge:

```text
Multi-agent apps with persistent memory and sensitive context.
```

Expansion:

- MCP gateway
- enterprise agent audit
- controlled tool execution
- production-action authorization
- agent trust infrastructure

## Slide 8: Moat

The moat is not YAML.

The moat is:

- least-context protocol
- disclosure transformations
- agent-to-agent memory ledger
- framework adapters
- policy and trust packs
- audit/replay of shared context

ContextGate becomes the memory trust layer for autonomous agent ecosystems.

## Slide 9: Why Me

I have spent 5 years building production backend, banking, and enterprise infrastructure systems.

At CBA, I worked on financial systems, GenAI integration, Kafka workflows, and customer-facing insurance/rewards services.

At DataCore, I built distributed enterprise storage infrastructure handling petabyte-scale deployments.

My MSc dissertation is already focused on Formal Agent Kernel: runtime validation for AI-agent actions and controlled disclosure.

ContextGate is the commercial product emerging from that research.

## Slide 10: Ask

I want to build ContextGate into the trust layer for autonomous agent ecosystems.

I am looking for:

- a technical/security/commercial cofounder
- design partners building multi-agent systems
- EF support to turn the research kernel into a company

Immediate next steps:

- build a LangGraph/OpenClaw-style memory workflow demo
- interview AI platform and security teams
- launch an open-source FAK Core
- validate willingness to pay for agent memory audit and disclosure governance

## One-Minute Pitch

AI agents are moving from solo assistants to teams. They now have persistent memory, tools, and the ability to delegate work to other agents.

But there is a hidden trust problem: agents need context to collaborate, yet they can easily overshare private user or enterprise memory.

Humans do not reveal everything they know to every collaborator. We share only what the other person needs. AI agents need the same boundary.

ContextGate is need-to-know memory for AI agents. It sits between agents and governs what context one agent can share with another. It can redact, summarize, deny, or require approval before memory is disclosed, and every decision is logged for audit and replay.

The underlying research kernel is my MSc work, Formal Agent Kernel, a runtime validator for AI-agent actions and controlled disclosure. The commercial product is ContextGate: a trust layer for agent-to-agent memory.

## Appendix: Competitive Map

| Category | What they control | What they miss |
| --- | --- | --- |
| LangGraph/CrewAI | Agent orchestration | Context disclosure governance |
| Guardrails | Output format/safety | Agent-to-agent memory sharing |
| OPA/Cedar | Generic authorization | Agent goals and memory semantics |
| MCP gateways | Tool access | Need-to-know memory |
| ContextGate | Agent-to-agent context disclosure | Focused wedge first |

## Appendix: Business Model

Open-source core:

- FAK runtime validator
- basic action and disclosure packs
- local audit logs
- policy tests

Paid product:

- hosted dashboard
- enterprise audit
- SSO/RBAC
- SIEM export
- policy registry
- approval workflows
- Slack/Jira integrations
- on-prem support
- enterprise action/trust packs

## Appendix: Wedge And Vision

Initial wedge:

```text
Need-to-know context sharing between agents.
```

Technical foundation:

```text
FAK runtime validator.
```

Future:

```text
Trust layer for agent communication, memory, and execution.
```

## References

- [YC: How to Pitch Your Company](https://www.ycombinator.com/blog/how-to-pitch-your-company/)
- [YC: Essential Startup Advice](https://www.ycombinator.com/blog/ycs-essential-startup-advice/)
- [YC: How to Apply](https://www.ycombinator.com/howtoapply)
- [Entrepreneur First](https://www.joinef.com/)
- [Entrepreneur First Apply](https://apply.joinef.com/)
- [OpenClaw Agents](https://openclawdoc.com/docs/agents/overview/)
- [OpenClaw Memory](https://docs.openclaw.ai/concepts/memory)
