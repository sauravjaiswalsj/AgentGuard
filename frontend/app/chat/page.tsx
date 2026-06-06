"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";

const AGENT_API = "http://localhost:8000";

type FakDecision = {
  tool: string;
  decision: string;
  reason: string;
  risk: string;
  matchedConstraints: string[];
  replayHash: string;
  latencyMs: number;
};

type AgentStep = {
  type: "tool_call";
  tool_name: string;
  tool_args: Record<string, string>;
  result: string | null;
  fak?: FakDecision;
};

type Message = {
  role: "user" | "assistant";
  content: string;
  steps?: AgentStep[];
  fakDecisions?: FakDecision[];
};

const SCENARIOS = [
  { label: "Safe staging deploy", prompt: "Deploy payments-service version 1.2.0 to staging." },
  { label: "Production deploy", prompt: "Deploy payments-service version 1.2.0 to production now." },
  { label: "Destructive shell", prompt: "Clean the server by running rm -rf /var/app/data" },
  { label: "Health check", prompt: "Check if payments-service is healthy." },
];

const DECISION_COLOR: Record<string, string> = {
  ALLOW: "#18794e",
  DENY: "#c2410c",
  REQUIRE_APPROVAL: "#a15c07",
};
const DECISION_BG: Record<string, string> = {
  ALLOW: "#dcfce7",
  DENY: "#fee2e2",
  REQUIRE_APPROVAL: "#fef3c7",
};
const DECISION_BORDER: Record<string, string> = {
  ALLOW: "#86efac",
  DENY: "#fca5a5",
  REQUIRE_APPROVAL: "#fde68a",
};
const DECISION_ICON: Record<string, string> = {
  ALLOW: "✅",
  DENY: "🚫",
  REQUIRE_APPROVAL: "⏳",
};

const TOOL_LABEL: Record<string, string> = {
  deploy_tool: "deploy_tool( )",
  shell_tool: "shell_tool( )",
};

function StepTrace({ steps }: { steps: AgentStep[] }) {
  const [open, setOpen] = useState(true);
  if (!steps || steps.length === 0) return null;

  return (
    <div style={{ margin: "10px 0 0", fontSize: 13 }}>
      <button
        onClick={() => setOpen(o => !o)}
        style={{
          background: "none", border: "none", cursor: "pointer",
          color: "#6b7280", fontSize: 12, padding: 0,
          display: "flex", alignItems: "center", gap: 4,
        }}
      >
        <span style={{ fontSize: 10 }}>{open ? "▼" : "▶"}</span>
        {open ? "Hide" : "Show"} agent trace ({steps.length} step{steps.length !== 1 ? "s" : ""})
      </button>

      {open && (
        <div style={{ marginTop: 8, display: "flex", flexDirection: "column", gap: 0 }}>
          {steps.map((step, i) => (
            <div key={i} style={{ display: "flex", gap: 0 }}>
              {/* Vertical line */}
              <div style={{ display: "flex", flexDirection: "column", alignItems: "center", marginRight: 10 }}>
                <div style={{
                  width: 28, height: 28, borderRadius: "50%",
                  background: step.fak ? (DECISION_BG[step.fak.decision] ?? "#f3f4f6") : "#f3f4f6",
                  border: `2px solid ${step.fak ? (DECISION_BORDER[step.fak.decision] ?? "#d1d5db") : "#d1d5db"}`,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 12, flexShrink: 0,
                }}>
                  {step.fak ? DECISION_ICON[step.fak.decision] : "🔧"}
                </div>
                {i < steps.length - 1 && (
                  <div style={{ width: 2, flex: 1, background: "#e5e7eb", minHeight: 16 }} />
                )}
              </div>

              {/* Step content */}
              <div style={{ flex: 1, paddingBottom: 16 }}>
                {/* Tool call header */}
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                  <span style={{
                    fontFamily: "monospace", fontSize: 12, fontWeight: 600,
                    background: "#1f2937", color: "#e5e7eb",
                    padding: "2px 8px", borderRadius: 4,
                  }}>
                    {TOOL_LABEL[step.tool_name] ?? step.tool_name}
                  </span>
                  {step.fak && (
                    <span style={{
                      fontSize: 11, fontWeight: 700, padding: "2px 8px", borderRadius: 20,
                      background: DECISION_BG[step.fak.decision],
                      color: DECISION_COLOR[step.fak.decision],
                      border: `1px solid ${DECISION_BORDER[step.fak.decision]}`,
                    }}>
                      FAK: {step.fak.decision}
                    </span>
                  )}
                </div>

                {/* Args */}
                <div style={{
                  background: "#f9fafb", border: "1px solid #e5e7eb",
                  borderRadius: 6, padding: "6px 10px", marginBottom: 4,
                }}>
                  <div style={{ fontSize: 11, color: "#6b7280", marginBottom: 3 }}>Arguments</div>
                  {Object.entries(step.tool_args).map(([k, v]) => (
                    <div key={k} style={{ display: "flex", gap: 8, fontSize: 12 }}>
                      <span style={{ color: "#6b7280", minWidth: 90 }}>{k}</span>
                      <span style={{ color: "#111827", fontFamily: "monospace" }}>{String(v)}</span>
                    </div>
                  ))}
                </div>

                {/* FAK reason */}
                {step.fak && (
                  <div style={{
                    background: DECISION_BG[step.fak.decision],
                    border: `1px solid ${DECISION_BORDER[step.fak.decision]}`,
                    borderRadius: 6, padding: "6px 10px", marginBottom: 4,
                    fontSize: 12,
                  }}>
                    <span style={{ fontWeight: 600, color: DECISION_COLOR[step.fak.decision] }}>
                      {step.fak.decision}
                    </span>
                    <span style={{ color: "#374151", marginLeft: 6 }}>{step.fak.reason}</span>
                    <span style={{ color: "#9ca3af", marginLeft: 6 }}>· {step.fak.latencyMs}ms · {step.fak.risk} risk</span>
                  </div>
                )}

                {/* Tool result */}
                {step.result && (
                  <div style={{
                    background: "#f0f9ff", border: "1px solid #bae6fd",
                    borderRadius: 6, padding: "6px 10px", fontSize: 12, color: "#0c4a6e",
                    fontFamily: "monospace",
                  }}>
                    {step.result}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function ChatPage() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [selectedDecision, setSelectedDecision] = useState<FakDecision | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function send(text: string) {
    if (!text.trim() || loading) return;
    setMessages(prev => [...prev, { role: "user", content: text }]);
    setInput("");
    setLoading(true);
    try {
      const res = await fetch(`${AGENT_API}/chat`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ message: text }),
      });
      const data = await res.json();
      setMessages(prev => [...prev, {
        role: "assistant",
        content: data.response,
        steps: data.steps ?? [],
        fakDecisions: data.fak_decisions ?? [],
      }]);
      if (data.fak_decisions?.length > 0) {
        setSelectedDecision(data.fak_decisions[data.fak_decisions.length - 1]);
      }
    } catch {
      setMessages(prev => [...prev, {
        role: "assistant",
        content: "❌ Could not reach the agent API. Make sure it is running on port 8000.",
      }]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="shell">
      {/* Sidebar */}
      <aside className="sidebar" style={{ display: "flex", flexDirection: "column", gap: 24 }}>
        <div>
          <h1 style={{ fontSize: 18, margin: "0 0 4px" }}>FAK Agent</h1>
          <p style={{ fontSize: 13, color: "#9ca3af", margin: 0 }}>Chat with a protected deploy agent</p>
        </div>
        <nav style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <Link href="/" style={{ color: "#9ca3af", fontSize: 14, textDecoration: "none", padding: "6px 0" }}>← Validation Demo</Link>
          <span style={{ color: "#f9fafb", fontSize: 14, padding: "6px 0", fontWeight: 600 }}>💬 Chat</span>
        </nav>
        <div>
          <p style={{ fontSize: 12, color: "#6b7280", margin: "0 0 8px", textTransform: "uppercase", letterSpacing: "0.05em" }}>Demo scenarios</p>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            {SCENARIOS.map(s => (
              <button
                key={s.label}
                onClick={() => send(s.prompt)}
                disabled={loading}
                style={{
                  background: "#1f2937", border: "1px solid #374151", borderRadius: 6,
                  color: "#e5e7eb", padding: "8px 10px", fontSize: 13,
                  textAlign: "left", cursor: loading ? "not-allowed" : "pointer",
                  opacity: loading ? 0.5 : 1,
                }}
              >
                {s.label}
              </button>
            ))}
          </div>
        </div>

        {/* Last FAK decision panel */}
        {selectedDecision && (
          <div style={{ marginTop: "auto" }}>
            <p style={{ fontSize: 12, color: "#6b7280", margin: "0 0 8px", textTransform: "uppercase", letterSpacing: "0.05em" }}>Last FAK decision</p>
            <div style={{
              background: DECISION_BG[selectedDecision.decision] ?? "#f3f4f6",
              border: `1px solid ${DECISION_BORDER[selectedDecision.decision] ?? "#d1d5db"}`,
              borderRadius: 6, padding: "10px 12px",
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <span style={{ fontSize: 16 }}>{DECISION_ICON[selectedDecision.decision]}</span>
                <span style={{ fontWeight: 700, fontSize: 13, color: DECISION_COLOR[selectedDecision.decision] }}>
                  {selectedDecision.decision}
                </span>
              </div>
              <p style={{ fontSize: 12, color: "#374151", margin: "6px 0 0" }}>{selectedDecision.reason}</p>
            </div>
            {selectedDecision.matchedConstraints?.length > 0 && (
              <div style={{ marginTop: 8 }}>
                <p style={{ fontSize: 11, color: "#6b7280", margin: "0 0 4px" }}>Matched constraints</p>
                {selectedDecision.matchedConstraints.map(c => (
                  <div key={c} style={{ fontSize: 11, color: "#9ca3af", fontFamily: "monospace", marginBottom: 2 }}>• {c}</div>
                ))}
              </div>
            )}
            <p style={{ fontSize: 11, color: "#6b7280", margin: "8px 0 0" }}>
              {selectedDecision.latencyMs}ms · {selectedDecision.risk} risk
            </p>
          </div>
        )}
      </aside>

      {/* Main chat area */}
      <main style={{ display: "flex", flexDirection: "column", height: "100vh", overflow: "hidden" }}>
        <div style={{ flex: 1, overflowY: "auto", padding: "28px 28px 0" }}>
          {messages.length === 0 && (
            <div style={{ textAlign: "center", color: "#9ca3af", marginTop: 80 }}>
              <p style={{ fontSize: 18, fontWeight: 600, color: "#374151" }}>FAK Deploy Agent</p>
              <p style={{ fontSize: 14 }}>Send a message or pick a scenario to see FAK in action.</p>
              <p style={{ fontSize: 13, color: "#9ca3af" }}>Each response shows the agent's step-by-step reasoning and FAK's decisions.</p>
            </div>
          )}

          {messages.map((msg, i) => (
            <div key={i} style={{ marginBottom: 24 }}>
              {msg.role === "user" ? (
                <div style={{ display: "flex", justifyContent: "flex-end" }}>
                  <div style={{
                    maxWidth: "70%", background: "#1f6feb", color: "#fff",
                    borderRadius: 12, padding: "12px 16px", fontSize: 14, lineHeight: 1.6,
                  }}>
                    {msg.content}
                  </div>
                </div>
              ) : (
                <div>
                  {/* Assistant bubble */}
                  <div style={{
                    maxWidth: "80%", background: "#ffffff", color: "#111827",
                    border: "1px solid #d9dee7", borderRadius: 12,
                    padding: "12px 16px", fontSize: 14, lineHeight: 1.6,
                  }}>
                    {msg.content}
                  </div>

                  {/* Step trace */}
                  {msg.steps && msg.steps.length > 0 && (
                    <div style={{ maxWidth: "80%", marginTop: 6 }}>
                      <StepTrace steps={msg.steps} />
                    </div>
                  )}

                  {/* FAK decision badges */}
                  {msg.fakDecisions && msg.fakDecisions.length > 0 && (
                    <div style={{ display: "flex", gap: 8, marginTop: 8, flexWrap: "wrap" }}>
                      {msg.fakDecisions.map((fd, j) => (
                        <button
                          key={j}
                          onClick={() => setSelectedDecision(fd)}
                          style={{
                            background: DECISION_BG[fd.decision] ?? "#f3f4f6",
                            border: `1px solid ${DECISION_BORDER[fd.decision] ?? "#d1d5db"}`,
                            borderRadius: 20, padding: "3px 10px", fontSize: 12,
                            fontWeight: 600, color: DECISION_COLOR[fd.decision] ?? "#374151",
                            cursor: "pointer",
                          }}
                        >
                          {DECISION_ICON[fd.decision]} FAK: {fd.decision} · {fd.tool}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}

          {loading && (
            <div style={{ display: "flex", alignItems: "center", gap: 8, color: "#9ca3af", fontSize: 14, marginBottom: 20 }}>
              <span style={{ animation: "spin 1s linear infinite", display: "inline-block" }}>⏳</span>
              Agent is thinking…
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        {/* Input bar */}
        <div style={{ padding: "16px 28px 24px", borderTop: "1px solid #d9dee7", background: "#f6f7f9" }}>
          <form
            onSubmit={e => { e.preventDefault(); send(input); }}
            style={{ display: "flex", gap: 10 }}
          >
            <input
              value={input}
              onChange={e => setInput(e.target.value)}
              placeholder="e.g. Deploy payments-service to production…"
              disabled={loading}
              style={{
                flex: 1, padding: "10px 14px", borderRadius: 8,
                border: "1px solid #d9dee7", fontSize: 14, background: "#ffffff",
                outline: "none",
              }}
            />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              style={{
                background: "#1f6feb", color: "#fff", border: "none",
                borderRadius: 8, padding: "10px 20px", fontSize: 14,
                fontWeight: 600, cursor: "pointer", opacity: loading ? 0.6 : 1,
              }}
            >
              Send
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}
