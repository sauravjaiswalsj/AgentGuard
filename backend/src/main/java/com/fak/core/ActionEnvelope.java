package com.fak.core;

import java.util.Map;

public record ActionEnvelope(
    Actor actor,
    Intent intent,
    Operation operation,
    Map<String, Object> context,
    String policyVersion
) {}
