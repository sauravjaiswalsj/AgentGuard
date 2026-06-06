"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { api } from "../lib/api";

type DecisionResponse = {
  actionId: string;
  decision: string;
  risk: string;
  reason: string;
  matchedConstraints: string[];
  policyVersion: string;
  replayHash: string;
  latencyMs: number;
  facts: Record<string, unknown>;
};

type AuditEvent = {
  id: string;
  createdAt: string;
  eventType: string;
  decision: string;
  policyVersion: string;
  replayHash: string;
};

const examples = {
  select: {
    actor: { agentId: "reporting_agent", framework: "langgraph", role: "database_reporter", trustLevel: "internal" },
    intent: { goal: "generate_report", declaredPurpose: "Generate monthly customer report" },
    operation: { type: "sql.query", target: "customers", parameters: { query: "SELECT id, name FROM customers" } },
    context: { environment: "production", userRole: "analyst", approvalState: "none" }
  },
  delete: {
    actor: { agentId: "reporting_agent", framework: "langgraph", role: "database_reporter", trustLevel: "internal" },
    intent: { goal: "generate_report", declaredPurpose: "Generate monthly customer report" },
    operation: { type: "sql.query", target: "customers", parameters: { query: "DELETE FROM customers WHERE last_login < '2022-01-01'" } },
    context: { environment: "production", userRole: "analyst", approvalState: "none" }
  },
  deploy: {
    actor: { agentId: "deploy_agent", framework: "langgraph", role: "deployment_operator", trustLevel: "internal" },
    intent: { goal: "deploy_production_release", declaredPurpose: "Ship tested build" },
    operation: { type: "devops.deploy", target: "payments-service", parameters: { version: "1.2.0" } },
    context: { environment: "production", userRole: "developer", approvalState: "none", changeWindow: false }
  },
  shell: {
    actor: { agentId: "deploy_agent", framework: "langgraph", role: "deployment_operator", trustLevel: "internal" },
    intent: { goal: "deploy_staging_release", declaredPurpose: "Clean staging files" },
    operation: { type: "shell.command", target: "server", parameters: { command: "rm -rf /var/app/data" } },
    context: { environment: "production", userRole: "developer", approvalState: "none" }
  }
};

export default function Home() {
  const [payload, setPayload] = useState(JSON.stringify(examples.select, null, 2));
  const [decision, setDecision] = useState<DecisionResponse | null>(null);
  const [audit, setAudit] = useState<AuditEvent[]>([]);
  const [metrics, setMetrics] = useState<Record<string, unknown>>({});
  const [policyTests, setPolicyTests] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);

  const decisionClass = useMemo(() => decision?.decision ?? "ALLOW", [decision]);

  async function refresh() {
    const [events, metricSnapshot] = await Promise.all([
      api<AuditEvent[]>("/audit/events"),
      api<Record<string, unknown>>("/metrics")
    ]);
    setAudit(events.slice(0, 8));
    setMetrics(metricSnapshot);
  }

  async function validate() {
    setError(null);
    try {
      const parsed = JSON.parse(payload);
      const result = await api<DecisionResponse>("/validate", {
        method: "POST",
        body: JSON.stringify(parsed)
      });
      setDecision(result);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Validation failed");
    }
  }

  async function runPolicyTests() {
    setPolicyTests(await api<Record<string, unknown>>("/policies/test", { method: "POST", body: "{}" }));
    await refresh();
  }

  async function replayFirst() {
    if (audit.length === 0) return;
    const result = await api<Record<string, unknown>>(`/replay/${audit[0].id}`, { method: "POST", body: "{}" });
    setDecision((result.replayed as DecisionResponse) ?? null);
    await refresh();
  }

  useEffect(() => {
    refresh().catch(() => undefined);
  }, []);

  return (
    <main className="shell">
      <aside className="sidebar">
        <div>
          <h1>Formal Agent Kernel</h1>
          <p>Config-driven runtime contract enforcement for AI agent actions before execution.</p>
          <p className="muted">Agents can reason freely. FAK governs what they execute.</p>
        </div>

        <nav style={{ display: "flex", flexDirection: "column", gap: 6, marginTop: 8 }}>
          <span style={{ fontSize: 12, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.05em" }}>Pages</span>
          <span style={{ color: "#f9fafb", fontSize: 14, fontWeight: 600, padding: "6px 0" }}>🔍 Validation Demo</span>
          <Link href="/chat" style={{ color: "#9ca3af", fontSize: 14, textDecoration: "none", padding: "6px 0" }}>
            💬 Agent Chat
          </Link>
          <Link href="/policies" style={{ color: "#9ca3af", fontSize: 14, textDecoration: "none", padding: "6px 0" }}>
            📋 Policy Manager
          </Link>
        </nav>
      </aside>

      <section className="main">
        <div className="grid">
          <div className="panel span-7">
            <h2>Action Proposal</h2>
            <div className="row">
              <button className="button" onClick={() => setPayload(JSON.stringify(examples.select, null, 2))}>Safe SQL</button>
              <button className="button" onClick={() => setPayload(JSON.stringify(examples.delete, null, 2))}>Production DELETE</button>
              <button className="button" onClick={() => setPayload(JSON.stringify(examples.deploy, null, 2))}>Prod Deploy</button>
              <button className="button" onClick={() => setPayload(JSON.stringify(examples.shell, null, 2))}>Shell Risk</button>
            </div>
            <textarea value={payload} onChange={(event) => setPayload(event.target.value)} />
            <div className="row">
              <button className="button primary" onClick={validate}>Validate</button>
              <button className="button" onClick={runPolicyTests}>Run Policy Tests</button>
              <button className="button" onClick={replayFirst}>Replay Latest</button>
            </div>
            {error && <p className="DENY pill">{error}</p>}
          </div>

          <div className="panel span-5">
            <h2>Decision</h2>
            {decision ? (
              <>
                <span className={`pill ${decisionClass}`}>{decision.decision}</span>
                <p>{decision.reason}</p>
                <p className="muted">Risk: {decision.risk} · Latency: {decision.latencyMs} ms</p>
                <p className="muted">Policy: {decision.policyVersion}</p>
                <pre>{JSON.stringify({ matchedConstraints: decision.matchedConstraints, replayHash: decision.replayHash }, null, 2)}</pre>
              </>
            ) : (
              <p className="muted">Validate an action to see the deterministic FAK decision.</p>
            )}
          </div>

          <div className="panel span-4">
            <h2>Metrics</h2>
            <pre>{JSON.stringify(metrics, null, 2)}</pre>
          </div>

          <div className="panel span-8">
            <h2>Audit Log</h2>
            <table>
              <thead>
                <tr><th>Time</th><th>Decision</th><th>Policy</th><th>Replay Hash</th></tr>
              </thead>
              <tbody>
                {audit.map((event) => (
                  <tr key={event.id}>
                    <td>{new Date(event.createdAt).toLocaleTimeString()}</td>
                    <td><span className={`pill ${event.decision}`}>{event.decision}</span></td>
                    <td>{event.policyVersion}</td>
                    <td>{event.replayHash.slice(0, 24)}...</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="panel span-12">
            <h2>Policy Test Results</h2>
            <pre>{JSON.stringify(policyTests ?? { status: "Not run yet" }, null, 2)}</pre>
          </div>
        </div>
      </section>
    </main>
  );
}
