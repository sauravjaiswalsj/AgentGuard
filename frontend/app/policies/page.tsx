"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

const FAK_API = "http://localhost:8080/api/v1";

type PolicyVersion = {
  id: number;
  versionId: string;
  description: string;
  author: string;
  status: "ACTIVE" | "DRAFT" | "ARCHIVED";
  createdAt: string;
  yamlContent: string;
};

const STATUS_COLOR: Record<string, string> = {
  ACTIVE:   "#18794e",
  DRAFT:    "#1d4ed8",
  ARCHIVED: "#6b7280",
};
const STATUS_BG: Record<string, string> = {
  ACTIVE:   "#dcfce7",
  DRAFT:    "#dbeafe",
  ARCHIVED: "#f3f4f6",
};

export default function PoliciesPage() {
  const [versions, setVersions]       = useState<PolicyVersion[]>([]);
  const [selected, setSelected]       = useState<PolicyVersion | null>(null);
  const [draftYaml, setDraftYaml]     = useState("");
  const [draftDesc, setDraftDesc]     = useState("");
  const [draftAuthor, setDraftAuthor] = useState("");
  const [showCreate, setShowCreate]   = useState(false);
  const [message, setMessage]         = useState<{ text: string; ok: boolean } | null>(null);
  const [loading, setLoading]         = useState(false);

  const [fetchError, setFetchError] = useState<string | null>(null);

  async function load() {
    setFetchError(null);
    try {
      const res = await fetch(`${FAK_API}/policies/versions`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data: PolicyVersion[] = await res.json();
      setVersions(data);
      if (data.length > 0) setSelected(prev => prev ?? data[0]);
    } catch (e) {
      setFetchError("Cannot reach FAK backend on port 8080. Is Spring Boot running?");
    }
  }

  useEffect(() => { load(); }, []);

  async function activate(id: number) {
    setLoading(true);
    setMessage(null);
    try {
      const res = await fetch(`${FAK_API}/policies/versions/${id}/activate`, { method: "POST" });
      const data = await res.json();
      setMessage({ text: data.message ?? "Activated.", ok: true });
      await load();
    } catch {
      setMessage({ text: "Failed to activate version.", ok: false });
    } finally {
      setLoading(false);
    }
  }

  async function createDraft() {
    if (!draftYaml.trim()) return;
    setLoading(true);
    setMessage(null);
    try {
      const res = await fetch(`${FAK_API}/policies/versions`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ yamlContent: draftYaml, description: draftDesc, author: draftAuthor || "user" }),
      });
      if (!res.ok) throw new Error(await res.text());
      setMessage({ text: "Draft created successfully.", ok: true });
      setShowCreate(false);
      setDraftYaml(""); setDraftDesc(""); setDraftAuthor("");
      await load();
    } catch (e) {
      setMessage({ text: `Failed to create draft: ${e instanceof Error ? e.message : e}`, ok: false });
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="shell">
      {/* Sidebar */}
      <aside className="sidebar" style={{ display: "flex", flexDirection: "column", gap: 20 }}>
        <div>
          <h1 style={{ fontSize: 18, margin: "0 0 4px" }}>Policy Manager</h1>
          <p style={{ fontSize: 13, color: "#9ca3af", margin: 0 }}>Version-controlled FAK policies</p>
        </div>
        <nav style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <Link href="/"       style={{ color: "#9ca3af", fontSize: 14, textDecoration: "none", padding: "5px 0" }}>🔍 Validation Demo</Link>
          <Link href="/chat"   style={{ color: "#9ca3af", fontSize: 14, textDecoration: "none", padding: "5px 0" }}>💬 Agent Chat</Link>
          <span style={{ color: "#f9fafb", fontSize: 14, fontWeight: 600, padding: "5px 0" }}>📋 Policy Manager</span>
        </nav>

        <div>
          <p style={{ fontSize: 12, color: "#6b7280", margin: "0 0 8px", textTransform: "uppercase", letterSpacing: "0.05em" }}>Versions</p>
          <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
            {versions.map(v => (
              <button
                key={v.id}
                onClick={() => setSelected(v)}
                style={{
                  background: selected?.id === v.id ? "#1f2937" : "transparent",
                  border: "1px solid " + (selected?.id === v.id ? "#374151" : "transparent"),
                  borderRadius: 6, padding: "8px 10px", textAlign: "left",
                  cursor: "pointer", color: "#e5e7eb",
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ fontSize: 12, fontFamily: "monospace" }}>{v.versionId}</span>
                  <span style={{
                    fontSize: 10, fontWeight: 700, padding: "1px 6px", borderRadius: 20,
                    background: STATUS_BG[v.status], color: STATUS_COLOR[v.status],
                  }}>{v.status}</span>
                </div>
                <div style={{ fontSize: 11, color: "#9ca3af", marginTop: 2 }}>{v.description?.slice(0, 40)}</div>
              </button>
            ))}
          </div>
        </div>

        <button
          onClick={() => setShowCreate(s => !s)}
          style={{
            marginTop: "auto", background: "#1f6feb", color: "#fff", border: "none",
            borderRadius: 8, padding: "10px", fontSize: 13, fontWeight: 600, cursor: "pointer",
          }}
        >
          + New Draft
        </button>
      </aside>

      {/* Main panel */}
      <main style={{ flex: 1, overflowY: "auto", padding: 28 }}>
        {fetchError && (
          <div style={{
            marginBottom: 16, padding: "12px 16px", borderRadius: 8, fontSize: 13,
            background: "#fef3c7", color: "#92400e", border: "1px solid #fde68a",
          }}>
            ⚠️ {fetchError} — <button onClick={load} style={{ background:"none", border:"none", color:"#1d4ed8", cursor:"pointer", fontSize:13, padding:0 }}>Retry</button>
          </div>
        )}
        {message && (
          <div style={{
            marginBottom: 16, padding: "10px 14px", borderRadius: 8, fontSize: 13,
            background: message.ok ? "#dcfce7" : "#fee2e2",
            color: message.ok ? "#18794e" : "#c2410c",
            border: `1px solid ${message.ok ? "#86efac" : "#fca5a5"}`,
          }}>
            {message.ok ? "✅" : "❌"} {message.text}
          </div>
        )}

        {/* Create draft panel */}
        {showCreate && (
          <div style={{ background: "#fff", border: "1px solid #d9dee7", borderRadius: 10, padding: 20, marginBottom: 20 }}>
            <h2 style={{ margin: "0 0 16px", fontSize: 16 }}>Create New Draft</h2>
            <div style={{ display: "flex", gap: 12, marginBottom: 12 }}>
              <input
                placeholder="Description (e.g. Tighten shell restrictions)"
                value={draftDesc}
                onChange={e => setDraftDesc(e.target.value)}
                style={{ flex: 2, padding: "8px 12px", border: "1px solid #d9dee7", borderRadius: 6, fontSize: 13 }}
              />
              <input
                placeholder="Author"
                value={draftAuthor}
                onChange={e => setDraftAuthor(e.target.value)}
                style={{ flex: 1, padding: "8px 12px", border: "1px solid #d9dee7", borderRadius: 6, fontSize: 13 }}
              />
            </div>
            <textarea
              value={draftYaml}
              onChange={e => setDraftYaml(e.target.value)}
              placeholder={"Paste your policy YAML here…\n\npolicyVersion: pol_2026_new\nagents:\n  ..."}
              style={{
                width: "100%", height: 300, fontFamily: "monospace", fontSize: 12,
                border: "1px solid #d9dee7", borderRadius: 6, padding: 12,
                boxSizing: "border-box", resize: "vertical",
              }}
            />
            <div style={{ display: "flex", gap: 10, marginTop: 10 }}>
              <button
                onClick={createDraft}
                disabled={loading || !draftYaml.trim()}
                style={{
                  background: "#1f6feb", color: "#fff", border: "none",
                  borderRadius: 6, padding: "8px 18px", fontSize: 13,
                  fontWeight: 600, cursor: "pointer",
                }}
              >
                Save Draft
              </button>
              <button
                onClick={() => setShowCreate(false)}
                style={{
                  background: "transparent", color: "#6b7280",
                  border: "1px solid #d9dee7", borderRadius: 6,
                  padding: "8px 18px", fontSize: 13, cursor: "pointer",
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        )}

        {/* Selected version detail */}
        {selected ? (
          <div style={{ background: "#fff", border: "1px solid #d9dee7", borderRadius: 10, padding: 24 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 20 }}>
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <h2 style={{ margin: 0, fontSize: 18, fontFamily: "monospace" }}>{selected.versionId}</h2>
                  <span style={{
                    fontSize: 12, fontWeight: 700, padding: "3px 10px", borderRadius: 20,
                    background: STATUS_BG[selected.status], color: STATUS_COLOR[selected.status],
                  }}>{selected.status}</span>
                </div>
                <p style={{ margin: "6px 0 0", fontSize: 13, color: "#6b7280" }}>{selected.description}</p>
                <p style={{ margin: "4px 0 0", fontSize: 12, color: "#9ca3af" }}>
                  by <strong>{selected.author}</strong> · {new Date(selected.createdAt).toLocaleString()}
                </p>
              </div>
              {selected.status !== "ACTIVE" && (
                <button
                  onClick={() => activate(selected.id)}
                  disabled={loading}
                  style={{
                    background: "#18794e", color: "#fff", border: "none",
                    borderRadius: 8, padding: "10px 20px", fontSize: 13,
                    fontWeight: 600, cursor: "pointer",
                  }}
                >
                  ⚡ Activate This Version
                </button>
              )}
              {selected.status === "ACTIVE" && (
                <span style={{
                  background: "#dcfce7", color: "#18794e", border: "1px solid #86efac",
                  borderRadius: 8, padding: "10px 20px", fontSize: 13, fontWeight: 600,
                }}>
                  ✅ Currently Active
                </span>
              )}
            </div>

            {/* What changes in this version vs. active */}
            <div>
              <h3 style={{ fontSize: 14, margin: "0 0 10px", color: "#374151" }}>Policy YAML</h3>
              <pre style={{
                background: "#0d1117", color: "#c9d1d9",
                borderRadius: 8, padding: 16, fontSize: 12,
                overflow: "auto", maxHeight: 500,
                fontFamily: "monospace", lineHeight: 1.6,
              }}>
                {selected.yamlContent}
              </pre>
            </div>
          </div>
        ) : (
          <div style={{ textAlign: "center", color: "#9ca3af", marginTop: 80 }}>
            <p style={{ fontSize: 16 }}>Select a policy version from the sidebar</p>
          </div>
        )}
      </main>
    </div>
  );
}
// policy manager ui
