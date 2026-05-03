# System Design

## Architecture

```text
Agent Framework
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

FAK is framework-agnostic because each integration converts framework-specific events into the same normalized envelope.

## Runtime Flow

1. Agent proposes an operation.
2. Adapter normalizes it into the FAK envelope.
3. Schema validator verifies required structure and domain schema.
4. Action/trust pack analyzer extracts normalized facts.
5. Constraint engine evaluates policies.
6. Decision engine resolves the final decision.
7. Approval engine creates pending approval records when required.
8. Audit logger records the decision and replay hash.
9. Executor, memory layer, or agent router proceeds only if the decision permits it.

## Core Modules

### `models`

Defines typed models:

- agent spec
- normalized envelope
- action/resource/operation
- context
- decision
- constraint
- audit event
- approval record
- replay record

### `config_loader`

Loads:

- `agent_specs.yml`
- `action_schema.yml`
- `constraints.yml`
- `approval_policies.yml`
- `policy_tests.yml`

It also computes policy version hashes.

### `schema_validator`

Validates:

- top-level envelope shape
- actor and intent fields
- operation payload
- pack-specific schemas
- context requirements

Invalid or unknown payloads fail closed unless explicitly configured otherwise.

### `constraint_engine`

Evaluates deterministic predicates against normalized facts.

Example facts:

- `operation.type = kubernetes.apply`
- `context.environment = production`
- `intent.goal = deploy_staging_release`
- `analysis.blast_radius = high`
- `resource.sensitivity = private`

### `decision_engine`

Combines constraint results using:

```text
DENY > REQUIRE_APPROVAL > REQUIRE_CONFIRMATION > ALLOW
```

For disclosure decisions:

```text
DENY_SHARE > SHARE_REDACTED > SHARE_SUMMARY_ONLY > SHARE_FULL
```

### `approval_engine`

Manages:

- approval creation
- approver requirements
- approval expiry
- approval decisions
- approval audit events

### `audit_logger`

Records:

- validation events
- execution events
- approval events
- denial events
- disclosure events
- delegation events
- config changes
- replay events

Audit events should include policy version and replay hash.

### `replay_engine`

Re-evaluates historical envelopes against the exact policy/config version used during the original decision.

### `policy_tests`

Runs fixture-based tests:

```yaml
name: production deploy outside change window requires approval
input:
  operation:
    type: kubernetes.apply
  context:
    environment: production
    change_window: false
expect:
  decision: REQUIRE_APPROVAL
```

### `adapters`

Initial adapters:

- LangGraph
- generic Python SDK
- FastAPI/webhook

Future adapters:

- LangChain
- CrewAI
- AutoGen
- OpenClaw
- MCP gateway

### `packs`

Initial packs:

- `sql`
- `devops_shell`
- `memory`
- `disclosure`
- `delegation`

## Initial API Endpoints

- `POST /v1/validate`
- `POST /v1/execute`
- `POST /v1/disclose`
- `POST /v1/delegate`
- `POST /v1/approvals/{approval_id}/decision`
- `GET /v1/audit/events`
- `POST /v1/replay`
- `POST /v1/policies/test`
- `GET /v1/specs/{agent_id}`
- `PUT /v1/specs/{agent_id}`

## Storage

MVP storage:

- SQLite for structured records
- JSONL hash-chain audit log for replayability

Future storage:

- PostgreSQL
- SIEM export
- policy registry
- hosted dashboard backend

## Failure Modes

FAK should fail closed for:

- unknown agent
- unknown operation type
- invalid schema
- missing required context
- malformed SQL or shell command
- missing policy version
- pack analyzer failure
- replay hash mismatch

FAK may return `REQUIRE_APPROVAL` instead of `DENY` for explicitly configured uncertainty cases.

