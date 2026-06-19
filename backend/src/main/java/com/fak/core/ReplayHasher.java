package com.fak.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Produces a deterministic SHA-256 hash of the serialised ActionEnvelope.
 * Stored in AuditEvent so any decision can be replayed from the original input.
 */
public class ReplayHasher {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String hash(ActionEnvelope envelope) {
        try {
            byte[] json = MAPPER.writeValueAsBytes(envelope);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(json);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash envelope", e);
        }
    }
}
