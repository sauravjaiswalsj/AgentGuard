package com.fak.config;

import com.fak.core.Decision;
import java.util.Map;

public record ConstraintRule(
    String id,
    Map<String, Object> when,
    Decision decision,
    String risk,
    String reason
) {}
