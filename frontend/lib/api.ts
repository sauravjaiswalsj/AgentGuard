/**
 * Typed FAK API client for the Next.js frontend.
 */
const BASE = process.env.NEXT_PUBLIC_FAK_API ?? "http://localhost:8080";

export interface ValidationDecision {
  decision: "ALLOW" | "DENY" | "REQUIRE_APPROVAL";
  reason: string;
  risk: string;
  matchedConstraints: string[];
  replayHash: string;
  latencyMs: number;
}

export interface PolicyVersionDto {
  id: number;
  versionId: string;
  description: string;
  author: string;
  status: "DRAFT" | "ACTIVE" | "ARCHIVED";
  createdAt: string;
  yamlContent?: string;
}

export async function listPolicyVersions(): Promise<PolicyVersionDto[]> {
  const r = await fetch(`${BASE}/api/v1/policies/versions`);
  if (!r.ok) throw new Error(`Failed to fetch policy versions: ${r.status}`);
  return r.json();
}

export async function getActivePolicyVersion(): Promise<PolicyVersionDto> {
  const r = await fetch(`${BASE}/api/v1/policies/versions/active`);
  if (!r.ok) throw new Error(`Failed to fetch active version: ${r.status}`);
  return r.json();
}

export async function activatePolicyVersion(id: number): Promise<PolicyVersionDto> {
  const r = await fetch(`${BASE}/api/v1/policies/versions/${id}/activate`, {method: "POST"});
  if (!r.ok) throw new Error(`Failed to activate version ${id}: ${r.status}`);
  return r.json();
}

export async function createDraftVersion(payload: {
  yamlContent: string;
  description: string;
  author: string;
}): Promise<PolicyVersionDto> {
  const r = await fetch(`${BASE}/api/v1/policies/versions`, {
    method: "POST",
    headers: {"content-type": "application/json"},
    body: JSON.stringify(payload),
  });
  if (!r.ok) throw new Error(`Failed to create draft: ${r.status}`);
  return r.json();
}
