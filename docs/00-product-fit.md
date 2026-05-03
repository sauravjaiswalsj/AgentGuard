# Product Fit: Formal Agent Kernel

## Positioning

FAK is a framework-agnostic runtime trust kernel for autonomous AI agents.

It validates what agents can execute, access, request, remember, disclose, and delegate before anything is executed or shared.

The commercial wedge is narrower:

```text
FAK prevents AI agents from making unsafe production changes.
```

The long-term product vision is broader:

```text
FAK is the policy and trust boundary between agent reasoning and real-world interaction.
```

## Problem

AI agents are moving from chat into execution. They can query databases, modify files, run shell commands, trigger deployments, call APIs, access memory, and delegate work to other agents. This creates risks that prompt instructions and output filters do not reliably prevent:

- rogue tool use
- unauthorized production changes
- data leakage
- unsafe database mutations
- secret or file exfiltration
- agent-to-agent over-sharing
- compliance and audit gaps
- unclear accountability for autonomous actions

The product insight is simple:

```text
Agents can reason freely, but they should not execute or disclose freely.
```

## Market Alignment

Existing systems validate the market, but leave a product gap for agent-native runtime trust.

- [LangGraph human-in-the-loop](https://docs.langchain.com/oss/python/langchain/human-in-the-loop) can pause tool calls and let a human approve, edit, or reject them. FAK must go beyond approval by adding goal awareness, action semantics, replay, and policy tests.
- [Open Policy Agent](https://www.openpolicyagent.org/docs/latest) is a mature general-purpose policy engine for structured data across microservices, Kubernetes, CI/CD, API gateways, and more. FAK should be agent-native rather than a generic policy sidecar.
- [Cedar](https://docs.cedarpolicy.com/auth/authorization.html) models authorization around principal, action, resource, and context. FAK extends the authorization idea toward autonomous agents by adding declared goals, action packs, disclosure, delegation, and replayable agent decisions.
- [GuardrailsAI](https://guardrailsai.com/guardrails/docs/concepts/guard) wraps LLM calls and validates generated outputs. FAK validates runtime actions and disclosures before they affect external systems.
- [OpenClaw agents](https://openclawdoc.com/docs/agents/overview/) demonstrate why persistent memory, tools, planning, and multi-agent workflows need runtime trust boundaries.
- [OpenClaw memory](https://docs.openclaw.ai/concepts/memory) shows how durable memory and memory search create a new disclosure problem: relevant context is not always safe context.

## Target Users

Primary buyers:

- Heads of platform engineering
- Security engineering leaders
- AI platform teams
- DevOps and cloud operations teams
- Compliance and governance teams

Primary users:

- Engineers building agents with LangGraph, LangChain, CrewAI, AutoGen, OpenClaw, MCP, or custom frameworks
- Platform teams exposing internal tools to AI agents
- Security teams defining runtime boundaries for agent actions

## Commercial Wedge

The first product should focus on production-impacting actions:

- SQL writes and destructive database operations
- production deployments
- shell commands
- file and repository mutations
- cloud or Kubernetes changes
- secret access
- external API mutations

This wedge is easier to demo, easier to buy, and more urgent than a generic "AI governance dashboard."

## Differentiation

FAK should not compete by claiming to approve or deny tool calls. That is table stakes.

The differentiator is:

```text
Goal-aware, context-aware, impact-aware runtime authorization for AI agents.
```

FAK asks:

```text
Is this action, by this agent, for this declared goal, in this context, with this impact, allowed under this policy version?
```

FAK also asks:

```text
Is this memory or context safe to disclose to this receiving agent for this purpose?
```

## Moat

The durable moat is not YAML. The moat is:

- domain action packs for SQL, DevOps/shell, code, memory, disclosure, and delegation
- goal-action consistency checks
- policy-versioned audit and replay
- policy test runner for developer trust
- framework adapters with low integration friction
- least-context disclosure for multi-agent systems

## Product Narrative

Use this narrative for README, demos, and early pitch material:

```text
FAK prevents AI agents from making unsafe production changes.

It sits between an agent and its tools. Every SQL query, shell command, deployment, API call, file mutation, memory access, or agent-to-agent disclosure is normalized into a structured envelope and validated against declarative policies before execution or sharing.
```

