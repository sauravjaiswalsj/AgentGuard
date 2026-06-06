package com.fak.config;

import java.util.List;

public record AgentSpec(String role, List<String> allowedGoals, List<String> allowedActions) {}
