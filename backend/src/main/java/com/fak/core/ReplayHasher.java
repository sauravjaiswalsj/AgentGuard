package com.fak.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;

@Service
public class ReplayHasher {
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  public String hash(ActionEnvelope envelope, Decision decision, String policyVersion) {
    try {
      String payload = mapper.writeValueAsString(envelope) + "|" + decision + "|" + policyVersion;
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder("sha256:");
      for (byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Unable to compute replay hash", ex);
    }
  }
}
