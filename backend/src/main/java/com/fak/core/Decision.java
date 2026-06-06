package com.fak.core;

public enum Decision {
  ALLOW(0), REQUIRE_CONFIRMATION(1), REQUIRE_APPROVAL(2), DENY(3);

  private final int precedence;

  Decision(int precedence) {
    this.precedence = precedence;
  }

  public int precedence() {
    return precedence;
  }

  public static Decision max(Decision a, Decision b) {
    return a.precedence >= b.precedence ? a : b;
  }
}
