package com.fak.core;

import java.util.Map;

/**
 * The concrete action the agent wants to perform.
 *
 * @param type       Operation type key, e.g. sql.query, devops.deploy, shell.command
 * @param target     Target resource (table name, service name, path …)
 * @param parameters Additional key-value parameters (query string, version, etc.)
 */
public record Operation(String type, String target, Map<String, Object> parameters) {}
