package com.fak.core;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Extracts a flat fact map from an ActionEnvelope.
 * Facts drive constraint evaluation in ConstraintEngine.
 *
 * <p>Domain-specific analyses (SQL classification, shell destructiveness) are
 * intentionally kept here for the dissertation prototype. A production kernel
 * would replace these with a pluggable {@code ActionAnalyzer} interface.</p>
 */
public class FactBuilder {

    // SQL keywords that indicate a write operation
    private static final Pattern SQL_WRITE = Pattern.compile(
        "^\s*(INSERT|UPDATE|DELETE|MERGE|UPSERT|REPLACE|TRUNCATE|DROP|ALTER|CREATE)\b",
        Pattern.CASE_INSENSITIVE);

    // SQL keywords that destroy data irreversibly
    private static final Pattern SQL_DESTRUCTIVE = Pattern.compile(
        "^\s*(DELETE|TRUNCATE|DROP)\b", Pattern.CASE_INSENSITIVE);

    // Shell commands that wipe or overwrite files
    private static final Pattern SHELL_DESTRUCTIVE = Pattern.compile(
        "\b(rm\s+-[a-z]*f|rm\s+-rf|dd\s+if=|mkfs\b|format\b|shred\b)",
        Pattern.CASE_INSENSITIVE);

    private static final Set<String> PII_COLUMNS = Set.of(
        "email", "phone", "ssn", "dob", "date_of_birth",
        "credit_card", "password", "national_id");

    public Map<String, Object> build(ActionEnvelope env, ValidationResult validation) {
        Map<String, Object> f = new LinkedHashMap<>();

        // ── Validation summary ──────────────────────────────
        f.put("validation.valid",        validation.valid());
        f.put("validation.knownAgent",   validation.knownAgent());
        f.put("validation.goalAllowed",  validation.goalAllowed());
        f.put("validation.actionAllowed",validation.actionAllowed());

        // ── Actor ───────────────────────────────────────────
        if (env.actor() != null) {
            f.put("actor.agentId",    env.actor().agentId());
            f.put("actor.role",       env.actor().role());
            f.put("actor.trustLevel", env.actor().trustLevel());
        }

        // ── Intent ──────────────────────────────────────────
        if (env.intent() != null) {
            f.put("intent.goal", env.intent().goal());
        }

        // ── Operation ───────────────────────────────────────
        if (env.operation() != null) {
            f.put("operation.type",   env.operation().type());
            f.put("operation.target", env.operation().target());
            analyseSql(env.operation(), f);
            analyseShell(env.operation(), f);
        }

        // ── Context ─────────────────────────────────────────
        if (env.context() != null) {
            env.context().forEach((k, v) -> f.put("context." + k, v));
        }

        return f;
    }

    private void analyseSql(Operation op, Map<String, Object> f) {
        if (!"sql.query".equals(op.type())) return;
        String q = param(op, "query");
        if (q == null) return;
        boolean write       = SQL_WRITE.matcher(q).find();
        boolean destructive = SQL_DESTRUCTIVE.matcher(q).find();
        boolean pii         = PII_COLUMNS.stream().anyMatch(c -> q.toLowerCase().contains(c));
        f.put("analysis.sql.write",       write);
        f.put("analysis.sql.destructive", destructive);
        f.put("analysis.sql.containsPii", pii);
    }

    private void analyseShell(Operation op, Map<String, Object> f) {
        if (!"shell.command".equals(op.type())) return;
        String cmd = param(op, "command");
        if (cmd == null) return;
        f.put("analysis.shell.destructive", SHELL_DESTRUCTIVE.matcher(cmd).find());
    }

    private String param(Operation op, String key) {
        if (op.parameters() == null) return null;
        Object v = op.parameters().get(key);
        return v == null ? null : v.toString();
    }
}
