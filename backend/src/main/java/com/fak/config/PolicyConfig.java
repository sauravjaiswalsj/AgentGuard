package com.fak.config;

import java.util.List;
import java.util.Map;

public record PolicyConfig(
    String policyVersion,
    Map<String, AgentSpec> agents,
    List<ConstraintRule> constraints,
    List<PolicyTestCase> policyTests
) {}
