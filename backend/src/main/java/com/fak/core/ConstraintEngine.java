package com.fak.core;

import com.fak.config.ConstraintRule;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ConstraintEngine {
  public List<ConstraintRule> matching(List<ConstraintRule> constraints, Map<String, Object> facts) {
    return constraints.stream().filter(rule -> matches(rule.when(), facts)).toList();
  }

  private boolean matches(Map<String, Object> expected, Map<String, Object> facts) {
    if (expected == null || expected.isEmpty()) {
      return false;
    }
    return expected.entrySet().stream().allMatch(entry -> valueMatches(resolve(facts, entry.getKey()), entry.getValue()));
  }

  private boolean valueMatches(Object actual, Object expected) {
    if (expected instanceof List<?> list) {
      return list.stream().anyMatch(item -> valueMatches(actual, item));
    }
    return String.valueOf(expected).equalsIgnoreCase(String.valueOf(actual));
  }

  private Object resolve(Map<String, Object> facts, String path) {
    Object cursor = facts;
    for (String part : path.split("\\.")) {
      if (!(cursor instanceof Map<?, ?> map)) {
        return null;
      }
      cursor = map.get(part);
    }
    return cursor;
  }
}
