"""SQL Reporting Agent demo — exercises FAK SQL constraints."""
import os, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "langgraph_deploy_agent"))
from fak_client import ActionEnvelope, Actor, Intent, Operation, validate

ACTOR = Actor(agentId="reporting_agent", role="database_reporter")

def check(label, query, context=None):
    env = ActionEnvelope(actor=ACTOR,
        intent=Intent(goal="generate_report", declaredPurpose="monthly report"),
        operation=Operation(type="sql.query", target="customers", parameters={"query": query}),
        context=context or {"environment": "production", "approvalState": "none"})
    r = validate(env)
    icon = {"ALLOW":"✅","DENY":"🚫","REQUIRE_APPROVAL":"⏳"}.get(r["decision"],"?")
    print(f"{icon} [{r['decision']}] {label}\n   {r['reason']} ({r['latencyMs']}ms)\n")

if __name__ == "__main__":
    check("Safe SELECT", "SELECT id, name FROM customers LIMIT 100")
    check("PII export", "SELECT id, email, phone FROM customers",
          {"environment":"production","destination":"external"})
    check("DELETE old records", "DELETE FROM customers WHERE last_login < '2022-01-01'")
    check("INSERT production write", "INSERT INTO audit_log(event) VALUES ('login')",
          {"environment":"production","approvalState":"none"})

# scenarios: ALLOW, DENY-PII, DENY-destructive, REQUIRE_APPROVAL-write
