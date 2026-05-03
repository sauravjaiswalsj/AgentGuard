# Demo Script

## Goal

Show a 10-minute FAK demo that communicates product value and dissertation contribution.

## Demo Flow

### 1. Setup

Show:

- agent spec
- action schema
- constraints
- running FAK API or SDK

Narrative:

```text
FAK sits between the agent and execution. The agent can propose actions, but FAK decides whether they can run.
```

### 2. Production Deploy Attempt

Agent proposes:

```json
{
  "agent_id": "deploy_agent",
  "goal": "deploy_staging_release",
  "operation": {
    "type": "kubernetes.apply",
    "target": "payments-service",
    "parameters": {
      "environment": "production"
    }
  },
  "context": {
    "user_role": "developer",
    "change_window": false,
    "approval_state": "none"
  }
}
```

Expected FAK response:

```text
REQUIRE_APPROVAL
```

### 3. Destructive SQL Attempt

Agent proposes:

```sql
DELETE FROM customers WHERE last_login < '2022-01-01';
```

Expected FAK response:

```text
DENY
```

Reason:

```text
Destructive production write is inconsistent with reporting goal and affects protected customer data.
```

### 4. Unsafe Shell Command

Agent proposes:

```text
rm -rf /var/app/data
```

Expected FAK response:

```text
REQUIRE_CONFIRMATION
```

### 5. Agent-To-Agent Memory Request

CareerAgent asks FinanceAgent for relocation context.

Expected FAK response:

```text
SHARE_REDACTED
```

Show:

- shared budget range
- shared risk preference
- withheld visa document number
- withheld exact bank balance
- withheld private emails

### 6. Audit Log

Show audit records containing:

- action id
- decision
- reason
- matched constraints
- policy version
- replay hash

### 7. Replay

Run replay for one denied action and one redacted disclosure.

Expected result:

```text
Replay decision matches original decision.
```

## Closing Message

```text
FAK demonstrates deterministic runtime enforcement for AI-agent actions and controlled disclosure. It is generic at the kernel level, domain-aware through packs, and auditable through policy-versioned replay.
```

