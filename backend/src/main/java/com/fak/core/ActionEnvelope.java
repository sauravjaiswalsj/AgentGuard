package com.fak.core;

import java.util.Map;

/**
 * The core data contract passed from an AI agent to FAK for validation.
 * An envelope fully describes *who* is acting, *what* they intend,
 * *what* they want to do, and *in what context*.
 */
public record ActionEnvelope(
    Actor actor,
    Intent intent,
    Operation operation,
    Map<String, Object> context
) {
    /** Convenience accessor: context value as string, or null. */
    public String ctx(String key) {
        Object v = context == null ? null : context.get(key);
        return v == null ? null : v.toString();
    }
}
