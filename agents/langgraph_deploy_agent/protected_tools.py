from fak_client import validate

# Captures FAK decisions during a single agent run
_session_decisions: list[dict] = []

def clear_decisions() -> None:
    _session_decisions.clear()

def get_decisions() -> list[dict]:
    return list(_session_decisions)


def deploy_to_cloud(service: str, version: str, environment: str) -> str:
    goal = "deploy_production_release" if environment == "production" else "deploy_staging_release"
    envelope = {
        "actor": {"agentId": "deploy_agent", "framework": "langgraph",
                  "role": "deployment_operator", "trustLevel": "internal"},
        "intent": {"goal": goal, "declaredPurpose": f"Deploy {service} version {version} to {environment}"},
        "operation": {"type": "devops.deploy", "target": service, "parameters": {"version": version}},
        "context": {"environment": environment, "approvalState": "none",
                    "changeWindow": False, "userRole": "developer"}
    }
    decision = validate(envelope)
    _session_decisions.append({
        "tool": "deploy_to_cloud",
        "args": {"service": service, "version": version, "environment": environment},
        **decision
    })
    d = decision.get("decision", "UNKNOWN")
    reason = decision.get("reason", "")
    if d == "ALLOW":
        return f"[SIMULATED] Successfully deployed {service} v{version} to {environment}. FAK: ALLOW."
    elif d == "REQUIRE_APPROVAL":
        return f"Deployment blocked pending approval. FAK: REQUIRE_APPROVAL. Reason: {reason}"
    else:
        return f"Deployment denied by FAK. Decision: {d}. Reason: {reason}"


def run_shell_command(command: str) -> str:
    envelope = {
        "actor": {"agentId": "deploy_agent", "framework": "langgraph",
                  "role": "deployment_operator", "trustLevel": "internal"},
        "intent": {"goal": "deploy_staging_release", "declaredPurpose": f"Run shell command: {command}"},
        "operation": {"type": "shell.command", "target": "host", "parameters": {"command": command}},
        "context": {"environment": "production", "approvalState": "none", "userRole": "developer"}
    }
    decision = validate(envelope)
    _session_decisions.append({
        "tool": "run_shell_command",
        "args": {"command": command},
        **decision
    })
    d = decision.get("decision", "UNKNOWN")
    reason = decision.get("reason", "")
    if d == "ALLOW":
        return f"[SIMULATED] Command executed: {command}. FAK: ALLOW."
    else:
        return f"Command blocked by FAK. Decision: {d}. Reason: {reason}"
