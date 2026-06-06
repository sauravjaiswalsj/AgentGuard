#!/usr/bin/env python3
import json
import urllib.request

API = "http://localhost:8080/api/v1/validate"


def validate(operation):
    payload = {
        "actor": {"agentId": "deploy_agent", "framework": "python-demo", "role": "deployment_operator", "trustLevel": "internal"},
        "intent": {"goal": "deploy_production_release", "declaredPurpose": "Ship a tested build"},
        "operation": operation,
        "context": {"environment": "production", "userRole": "developer", "approvalState": "none", "changeWindow": False},
    }
    request = urllib.request.Request(API, data=json.dumps(payload).encode(), headers={"content-type": "application/json"})
    with urllib.request.urlopen(request) as response:
        print(json.dumps(json.loads(response.read()), indent=2))


if __name__ == "__main__":
    validate({"type": "devops.deploy", "target": "payments-service", "parameters": {"version": "1.2.0"}})
    validate({"type": "shell.command", "target": "server", "parameters": {"command": "rm -rf /var/app/data"}})
