package com.fak.core;

/** Possible decisions returned by the FAK kernel. */
public enum Decision {
    ALLOW,
    DENY,
    REQUIRE_APPROVAL;

    public boolean isBlocking() {
        return this == DENY;
    }

    public boolean needsHuman() {
        return this == REQUIRE_APPROVAL;
    }
}
