package com.fak.config;

import com.fak.core.ActionEnvelope;
import com.fak.core.Decision;

public record PolicyTestCase(String name, ActionEnvelope envelope, Decision expectDecision) {}
