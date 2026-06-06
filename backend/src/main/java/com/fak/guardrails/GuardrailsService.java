package com.fak.guardrails;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GuardrailsService {
  private static final List<String> FLAGS = List.of("ignore previous", "bypass policy", "disable guardrail");

  public Map<String, Object> inspect(String text) {
    String lower = text == null ? "" : text.toLowerCase();
    List<String> matched = FLAGS.stream().filter(lower::contains).toList();
    return Map.of("passed", matched.isEmpty(), "matchedHeuristics", matched);
  }
}
