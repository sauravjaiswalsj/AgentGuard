"""
FAK client — sends an ActionEnvelope to the FAK kernel and returns the decision.
Called by protected_tools.py before any real tool execution.
"""
import os, requests
from dataclasses import dataclass, asdict
from typing import Any

FAK_URL = os.getenv("FAK_URL", "http://localhost:8080")


@dataclass
class Actor:
    agentId: str
    role: str
    framework: str = "langgraph"
    trustLevel: str = "internal"


@dataclass
class Intent:
    goal: str
    declaredPurpose: str = ""


@dataclass
class Operation:
    type: str
    target: str
    parameters: dict[str, Any] = None

    def __post_init__(self):
        if self.parameters is None:
            self.parameters = {}


@dataclass
class ActionEnvelope:
    actor: Actor
    intent: Intent
    operation: Operation
    context: dict[str, Any] = None

    def __post_init__(self):
        if self.context is None:
            self.context = {}

    def to_dict(self):
        return {
            "actor": asdict(self.actor),
            "intent": asdict(self.intent),
            "operation": asdict(self.operation),
            "context": self.context,
        }


def validate(envelope: ActionEnvelope) -> dict:
    """POST the envelope to FAK and return the parsed JSON decision."""
    resp = requests.post(f"{FAK_URL}/api/v1/validate", json=envelope.to_dict(), timeout=5)
    resp.raise_for_status()
    return resp.json()
