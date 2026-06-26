package com.fak.core;

public record ValidationResult(boolean valid, boolean knownAgent, boolean goalAllowed, boolean actionAllowed) {
    public static ValidationResult invalid() {
        return new ValidationResult(false, false, false, false);
    }
}
