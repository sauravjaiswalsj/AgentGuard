package com.fak.core;

/**
 * Represents the AI agent submitting an action for validation.
 *
 * @param agentId   Unique identifier for the agent (must match agents registry).
 * @param role      Declared role (e.g. database_reporter, deployment_operator).
 * @param framework Runtime framework (langgraph, autogen, crewai …).
 * @param trustLevel Internal trust classification (internal / external).
 */
public record Actor(String agentId, String role, String framework, String trustLevel) {}
