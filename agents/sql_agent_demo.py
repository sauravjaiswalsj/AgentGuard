#!/usr/bin/env python3
import json
import urllib.request

API = "http://localhost:8080/api/v1/validate"


def validate(query: str):
    payload = {
        "actor": {"agentId": "reporting_agent", "framework": "python-demo", "role": "database_reporter", "trustLevel": "internal"},
        "intent": {"goal": "generate_report", "declaredPurpose": "Generate a customer report"},
        "operation": {"type": "sql.query", "target": "customers", "parameters": {"query": query}},
        "context": {"environment": "production", "userRole": "analyst", "approvalState": "none"},
    }
    request = urllib.request.Request(API, data=json.dumps(payload).encode(), headers={"content-type": "application/json"})
    with urllib.request.urlopen(request) as response:
        print(json.dumps(json.loads(response.read()), indent=2))


if __name__ == "__main__":
    validate("SELECT id, name FROM customers")
    validate("DELETE FROM customers WHERE last_login < '2022-01-01'")
