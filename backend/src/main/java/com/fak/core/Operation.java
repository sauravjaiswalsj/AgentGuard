package com.fak.core;

import java.util.Map;

public record Operation(String type, String target, Map<String, Object> parameters) {}
