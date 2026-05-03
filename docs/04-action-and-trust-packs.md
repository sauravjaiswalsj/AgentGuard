# Action And Trust Packs

## Pack Architecture

FAK Core is domain-agnostic. Packs make it domain-aware.

Each pack provides:

- action schemas
- analyzer logic
- normalized facts
- sample constraints
- sample policy tests
- demo traces

The pack contract:

```text
normalized_envelope -> analyzer -> normalized_facts
normalized_facts + constraints -> decision
```

## Initial Packs

### `sql`

Purpose:

- validate database-agent actions
- classify query operation
- detect destructive SQL
- detect protected tables and PII export
- estimate or require review for row impact

Example facts:

- `sql.operation = DELETE`
- `sql.tables = ["customers"]`
- `sql.contains_pii = true`
- `context.environment = production`

Example decisions:

- `ALLOW` for safe read-only SELECT on approved data
- `REQUIRE_APPROVAL` for production writes
- `DENY` for destructive production operations inconsistent with the agent goal

### `devops_shell`

Purpose:

- validate deployment, cloud, Kubernetes, Terraform, and shell actions
- classify blast radius
- detect production environment changes
- detect destructive commands
- require approval for high-risk operations

Example facts:

- `operation.type = terraform.apply`
- `operation.target = payments-infra`
- `context.environment = production`
- `analysis.blast_radius = high`

Example decisions:

- `ALLOW` staging deploys
- `REQUIRE_APPROVAL` production deploys
- `DENY` destructive commands without safeguards

### `memory`

Purpose:

- validate whether an agent can read, write, or search memory
- enforce sensitivity labels
- restrict memory by workspace, channel, user, agent role, and purpose

Example facts:

- `resource.type = memory_item`
- `resource.sensitivity = private`
- `context.channel = agent_to_agent`

### `disclosure`

Purpose:

- filter what one agent can share with another
- redact sensitive fields
- convert raw context into summary-only context
- enforce user consent for sensitive disclosure

Example decisions:

- `SHARE_FULL`
- `SHARE_REDACTED`
- `SHARE_SUMMARY_ONLY`
- `DENY_SHARE`

### `delegation`

Purpose:

- validate whether one agent can assign a subtask to another
- ensure delegated work is compatible with sender role, receiver role, user goal, and policy
- restrict delegation loops or privilege escalation through another agent

Example:

```text
ResearchAgent can ask FinanceAgent for a budget summary.
ResearchAgent cannot ask FinanceAgent to execute a payment.
```

## Future Commercial Packs

Do not build these in V1. They are future expansion areas:

- finance
- healthcare
- SOC/security operations
- ERP
- procurement
- HR workflows
- full enterprise workflow automation
- Kubernetes admission-controller style pack
- Terraform plan deep-analysis pack

