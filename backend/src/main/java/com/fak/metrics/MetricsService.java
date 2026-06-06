package com.fak.metrics;

import com.fak.core.Decision;
import com.fak.core.ValidationDecision;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {
  private final List<Long> latencies = new ArrayList<>();
  private final Map<Decision, Integer> decisions = new EnumMap<>(Decision.class);

  public synchronized void record(ValidationDecision decision) {
    latencies.add(decision.latencyMs());
    decisions.merge(decision.decision(), 1, Integer::sum);
  }

  public synchronized Map<String, Object> snapshot() {
    List<Long> sorted = latencies.stream().sorted().toList();
    long median = percentile(sorted, 0.5);
    long p95 = percentile(sorted, 0.95);
    return Map.of(
        "totalValidations", latencies.size(),
        "medianLatencyMs", median,
        "p95LatencyMs", p95,
        "decisionCounts", decisions);
  }

  private long percentile(List<Long> sorted, double p) {
    if (sorted.isEmpty()) {
      return 0;
    }
    int index = Math.min(sorted.size() - 1, (int) Math.ceil(sorted.size() * p) - 1);
    return sorted.get(index);
  }
}
