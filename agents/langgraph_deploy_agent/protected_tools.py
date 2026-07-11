"""
FAK-protected tools for the LangGraph deploy agent.

Each tool function:
  1. Builds an ActionEnvelope describing the action.
  2. Calls the FAK kernel via fak_client.validate().
  3. Returns early if the decision is DENY or REQUIRE_APPROVAL.
  4. Only executes the real action on ALLOW.

FAK decisions from this session are stored in _session_decisions
so the FastAPI bridge can expose them to the frontend.
"""
import time
from fak_client import ActionEnvelope, Actor, Intent, Operation, validate

_ACTOR = Actor(agentId="deploy_agent", role="deployment_operator")
_session_decisions: list[dict] = []


def clear_decisions():
    _session_decisions.clear()


def get_decisions() -> list[dict]:
    return list(_session_decisions)


def _record(tool: str, decision: dict):
    _session_decisions.append({"tool": tool, **decision})


def deploy_to_cloud(service: str, version: str, environment: str) -> str:
    t0 = time.monotonic()
    goal = ("deploy_production_release"
            if environment == "production" else "deploy_staging_release")

    envelope = ActionEnvelope(
        actor=_ACTOR,
        intent=Intent(goal=goal, declaredPurpose=f"deploy {service} {version}"),
        operation=Operation(
            type="devops.deploy",
            target=service,
            parameters={"version": version},
        ),
        context={
            "environment": environment,
            "approvalState": "none",
            "changeWindow": False,
        },
    )

    result = validate(envelope)
    result["latencyMs"] = int((time.monotonic() - t0) * 1000)
    _record("deploy_to_cloud", result)

    decision = result.get("decision", "DENY")
    if decision == "ALLOW":
        return f"✅ Deployed {service} v{version} to {environment}."
    if decision == "REQUIRE_APPROVAL":
        return f"⏳ Deployment requires human approval. Reason: {result.get('reason')}"
    return f"🚫 Deploy blocked by FAK. Reason: {result.get('reason')}"


def run_shell_command(command: str) -> str:
    t0 = time.monotonic()
    envelope = ActionEnvelope(
        actor=_ACTOR,
        intent=Intent(goal="deploy_staging_release", declaredPurpose="maintenance"),
        operation=Operation(
            type="shell.command",
            target="host",
            parameters={"command": command},
        ),
        context={"environment": "production"},
    )

    result = validate(envelope)
    result["latencyMs"] = int((time.monotonic() - t0) * 1000)
    _record("run_shell_command", result)

    decision = result.get("decision", "DENY")
    if decision == "ALLOW":
        return f"✅ Command executed: {command}"
    if decision == "REQUIRE_APPROVAL":
        return f"⏳ Command requires approval. Reason: {result.get('reason')}"
    return f"🚫 Command blocked by FAK. Reason: {result.get('reason')}"
